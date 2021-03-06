package net.feichti.microjavaeditor.microjava;

import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.feichti.microjavaeditor.MJContentOutlinePage;
import net.feichti.microjavaeditor.MicroJavaEditorPlugin;
import net.feichti.microjavaeditor.antlr4.MicroJavaBaseListener;
import net.feichti.microjavaeditor.antlr4.MicroJavaLexer;
import net.feichti.microjavaeditor.antlr4.MicroJavaListener;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ClassDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ConstDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.MethodDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ParamContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ProgContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;
import net.feichti.microjavaeditor.symtab.ClassSymbol;
import net.feichti.microjavaeditor.symtab.ConstantSymbol;
import net.feichti.microjavaeditor.symtab.MethodSymbol;
import net.feichti.microjavaeditor.symtab.Scope;
import net.feichti.microjavaeditor.symtab.Symbol;
import net.feichti.microjavaeditor.symtab.SymbolTable;
import net.feichti.microjavaeditor.symtab.Type;
import net.feichti.microjavaeditor.symtab.VariableSymbol;
import net.feichti.microjavaeditor.util.SourceRegion;
import net.feichti.microjavaeditor.util.VarDeclWrapper;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Represents a model of a MicroJava file and is a tree content provider.
 * <p>
 * This class is used as the content provider for the {@link MJContentOutlinePage}, when the input is set the
 * file is parsed and the {@link ProgContext} for the file as well as the parser can be accessed.<br>
 * This class also provides methods to work with the generated parse tree.
 * 
 * @TODO Parsing errors are collected and can be accessed, e.g. for display to the user.
 * @author Peter
 */
public class MJFileModel implements ITreeContentProvider
{
	/**
	 * The possible types of variable declarations.
	 */
	public static enum VariableKind
	{
		/**
		 * Declaration represents a global variable.
		 */
		GLOBAL(MicroJavaEditorPlugin.IMG_VARIABLE),
		/**
		 * Declaration represents a local variable.
		 */
		LOCAL(MicroJavaEditorPlugin.IMG_LOCAL),
		/**
		 * Declaration represents a class field.
		 */
		FIELD(MicroJavaEditorPlugin.IMG_FIELD);
		
		/**
		 * The key for getting the outline view icon for this variable kind.
		 * 
		 * @see MicroJavaEditorPlugin#getImage(String)
		 * @see MicroJavaEditorPlugin#getImageDescriptor(String)
		 */
		public final String imageKey;
		
		/**
		 * Get a {@link VariableKind} for the specified declaration.
		 * 
		 * @param decl The {@link VarDeclContext}
		 * @return A variable kind, or {@code null} if the type cannot be determined
		 */
		public static VariableKind forDeclaration(VarDeclContext decl) {
			if(decl.parent instanceof ClassDeclContext) {
				return FIELD;
			} else if(decl.parent instanceof MethodDeclContext) {
				return LOCAL;
			} else if(decl.parent instanceof ProgContext) {
				return GLOBAL;
			} else {
				return null;
			}
		}
		
		private VariableKind(String imgKey) {
			imageKey = imgKey;
		}
	}
	
	/**
	 * Base class for errors encountered during parsing.
	 */
	public static abstract class ParserError
	{
		public final int line;
		public final int col;
		public final String message;
		public final Object offendingSymbol;
		
		public ParserError(int line, int col, String message, Object offendingSymbol) {
			this.line = line;
			this.col = col;
			this.message = message;
			this.offendingSymbol = offendingSymbol;
		}
		
		@Override
		public String toString() {
			return String.format("line %d:%d %s", line, col, message);
		}
	}
	
	/**
	 * Represents a single syntax error reported by the parser.
	 */
	public static class SyntaxError extends ParserError
	{
		public SyntaxError(int line, int col, String message, Token token) {
			super(line, col, message, token);
		}
		
		public Token getToken() {
			return (Token)offendingSymbol;
		}
	}
	
	/**
	 * Represents a single error encountered during semantic analysis.
	 */
	public static class SemanticError extends ParserError
	{
		public SemanticError(int line, int col, String message, ParseTree parseTreeNode) {
			super(line, col, message, parseTreeNode);
		}
		
		public ParseTree getParseTreeNode() {
			return (ParseTree)offendingSymbol;
		}
	}
	
	/**
	 * Adds reported syntax errors to {@code mSyntaxErrors}.
	 */
	private class ParserErrorListener extends BaseErrorListener
	{
		public ParserErrorListener() {
			
		}
		
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			mErrors.add(new SyntaxError(line, charPositionInLine, msg, (Token)offendingSymbol));
		}
	}
	
	/**
	 * A {@link MicroJavaListener} that builds a symbol table when the program is parsed.
	 */
	protected class SymbolTableBuilder extends MicroJavaBaseListener
	{
		final ParseTreeProperty<Scope> mScopes = new ParseTreeProperty<>();
		final Map<Symbol, ParseTree> mDeclarations = new IdentityHashMap<>();
		final Scope mGlobalScope;
		Scope mCurrentScope;
		
		public SymbolTableBuilder(SymbolTable tab) {
			mGlobalScope = tab.getUniverse();
			mCurrentScope = mGlobalScope;
			tab.setScopes(mScopes);
			tab.setDeclarations(mDeclarations);
		}
		
		private VariableSymbol defineVar(MicroJavaParser.TypeContext ctx, Token ident) {
			Type type = mCurrentScope.resolveType(ctx.Ident().getText());
			VariableSymbol var = new VariableSymbol(ident.getText(), type);
			mCurrentScope.define(var);
			return var;
		}
		
		@Override
		public void enterClassDecl(ClassDeclContext ctx) {
			String name = ctx.Ident().getText();
			ClassSymbol scope = new ClassSymbol(name, mCurrentScope);
			mDeclarations.put(scope, ctx);
			mScopes.put(ctx, scope);
			mCurrentScope.define(scope);
			mCurrentScope = scope;
		}
		
		@Override
		public void exitClassDecl(ClassDeclContext ctx) {
			mCurrentScope = mCurrentScope.getParent();
		}
		
		@Override
		public void enterMethodDecl(MethodDeclContext ctx) {
			String name = ctx.Ident().getText();
			Type type = null;
			if(ctx.type() != null) {
				type = mCurrentScope.resolveType(ctx.type().Ident().getText());
			} else if(ctx.getToken(MicroJavaParser.VOID, 0) != null) {
				type = mCurrentScope.resolveType("void");
			}
			MethodSymbol scope = new MethodSymbol(name, type, mCurrentScope);
			mDeclarations.put(scope, ctx);
			mScopes.put(ctx, scope);
			mCurrentScope = scope;
		}
		
		@Override
		public void exitMethodDecl(MethodDeclContext ctx) {
			mCurrentScope = mCurrentScope.getParent();
		}
		
		@Override
		public void exitParam(ParamContext ctx) {
			Symbol param = defineVar(ctx.type(), ctx.Ident().getSymbol());
			mDeclarations.put(param, ctx.getParent());
		}
		
		@Override
		public void exitVarDecl(VarDeclContext ctx) {
			for(TerminalNode ident : ctx.Ident()) {
				Symbol var = defineVar(ctx.type(), ident.getSymbol());
				mDeclarations.put(var, ctx);
			}
		}
		
		@Override
		public void exitConstDecl(ConstDeclContext ctx) {
			String name = ctx.Ident().getText();
			Type type = null;
			if(ctx.type() != null && ctx.type().Ident() != null) {
				type = mCurrentScope.resolveType(ctx.type().Ident().getText());
			}
			Object value = ctx.literal().getText();
			ConstantSymbol cons = new ConstantSymbol(name, type, value);
			mDeclarations.put(cons, ctx);
			mCurrentScope.define(cons);
		}
	}
	
	public static final String ELEMENTS = "__microjava_elements";
	
	final List<ParserError> mErrors = new LinkedList<>();
	private final IPositionUpdater mPositionUpdater;
	private final IDocumentProvider mDocumentProvider;
	
	/** The parser used to parse the current document, kept even on error. */
	private MicroJavaParser mParser = null;
	/** The context on successful parse, {@code null} otherwise. */
	private ProgContext mRoot = null;
	/** The list of tokens for position search on successful parse, {@code null} otherwise. */
	private TerminalNode[] mTokens;
	/** The symbol table constructed when walking the parse tree. */
	private SymbolTable mSymbolTable;
	/** The current document. */
	private IDocument mDocument = null;
	/** The resource for the current input. */
	private IResource mInputResource = null;
	
	/**
	 * Initialize a new model with the specified content provider.
	 * 
	 * @param documentProvider The document provider
	 */
	public MJFileModel(IDocumentProvider documentProvider) {
		mPositionUpdater = new DefaultPositionUpdater(ELEMENTS);
		mDocumentProvider = documentProvider;
	}
	
	/**
	 * Parse the specified document and set {@link #mRoot} and {@link #mParser}.
	 * <p>
	 * This method also populates the token list ({@link #mTokens} for token position search), adds markers
	 * for encountered syntax and semantic errors (which are added to {@link #mErrors}) and builds a symbol
	 * table.
	 */
	private void parse(IDocument doc) {
		try {
			MicroJavaLexer lex = new MicroJavaLexer(new ANTLRInputStream(doc.get()));
			CommonTokenStream tokens = new CommonTokenStream(lex);
			mParser = new MicroJavaParser(tokens);
			// We don't want syntax errors printed to the console, so remove the default ConsoleErrorListener
			mParser.removeErrorListeners();
			mParser.addErrorListener(new ParserErrorListener());
			
			// Parse!
			mRoot = mParser.prog();
			
			// Collect terminal nodes for position search
			List<TerminalNode> tmp = collectTerminalNodes(mRoot);
			if(tmp.size() > 1) {
				// We need two tokens for position search, a program with one token makes no sense anyway
				mTokens = tmp.toArray(new TerminalNode[0]);
			} else {
				mRoot = null;
				mTokens = null;
			}
			
			System.out.println("parse finished");
		} catch(RecognitionException ex) {
			System.out.println("parse failed:");
			ex.printStackTrace();
			mRoot = null;
			mTokens = null;
		}
		
		if(mRoot != null) {
			mSymbolTable = new SymbolTable();
			ParseTreeWalker.DEFAULT.walk(new SymbolTableBuilder(mSymbolTable), mRoot);
			
			try {
				final int docLength = mDocument.getLength();
				for(ParserError err : mErrors) {
					int errLength;
					if(err instanceof SyntaxError) {
						errLength = ((SyntaxError)err).getToken().getText().length();
					} else if(err instanceof SemanticError) {
						errLength = ((SemanticError)err).getParseTreeNode().getText().length();
					} else {
						errLength = err.offendingSymbol.toString().length();
					}
					
					IMarker m = mInputResource.createMarker(IMarker.PROBLEM);
					m.setAttribute(IMarker.LINE_NUMBER, err.line);
					m.setAttribute(IMarker.MESSAGE, err.message);
					m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					try {
						int offset = mDocument.getLineOffset(err.line - 1);
						if(offset + err.col < docLength) {
							m.setAttribute(IMarker.CHAR_START, offset + err.col);
							m.setAttribute(IMarker.CHAR_END, offset + err.col + errLength);
						} else {
							m.setAttribute(IMarker.CHAR_START, docLength - 1);
							m.setAttribute(IMarker.CHAR_END, docLength);
						}
					} catch(BadLocationException ex) {
						// Ignore, no exact position available
					}
				}
			} catch(CoreException ex) {
				System.err.println("Failed to create problem marker:");
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public void dispose() {
		clear();
	}

	private void clear() {
		mRoot = null;
		mParser = null;
		mDocument = null;
		mTokens = null;
		mSymbolTable = null;
		mErrors.clear();
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(oldInput != null) {
			if(mDocument != null) {
				try {
					mDocument.removePositionCategory(ELEMENTS);
				} catch(BadPositionCategoryException ex) {
					
				}
				mDocument.removePositionUpdater(mPositionUpdater);
			}
			if(mInputResource != null) {
				try {
					mInputResource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				} catch(CoreException ex) {
					
				}
			}
		}
		
		clear();
		
		if(newInput != null) {
			mDocument = mDocumentProvider.getDocument(newInput);
			if(mDocument != null) {
				mDocument.addPositionCategory(ELEMENTS);
				mDocument.addPositionUpdater(mPositionUpdater);
				parse(mDocument);
			}
			mInputResource = ResourceUtil.getResource((IEditorInput)newInput);
		}
	}
	
	@Override
	public Object[] getElements(Object input) {
		if(mRoot == null) {
			return new Object[] { "Parser error." };
		}
		
		List<Object> elems = new ArrayList<>();
		elems.add(mRoot);
		elems.addAll(mRoot.classDecl());
		elems.addAll(mRoot.constDecl());
		addVarDecls(elems, mRoot.varDecl());
		elems.addAll(mRoot.methodDecl());
		return elems.toArray();
	}
	
	@Override
	public Object[] getChildren(Object parent) {
		List<Object> elems = new ArrayList<>();
		if(parent instanceof ClassDeclContext) {
			ClassDeclContext clazz = (ClassDeclContext)parent;
			addVarDecls(elems, clazz.varDecl());
			
		} else if(parent instanceof MethodDeclContext) {
			MethodDeclContext method = (MethodDeclContext)parent;
			addVarDecls(elems, method.varDecl());
			
		}
		return elems.toArray();
	}
	
	/**
	 * Add {@link VarDeclWrapper}s for the specified {@link VarDeclContext}s to the list.
	 * 
	 * @param target The list to add wrappers to
	 * @param varDecls The variable declarations
	 */
	private static void addVarDecls(List<Object> target, List<VarDeclContext> varDecls) {
		for(VarDeclContext decl : varDecls) {
			int num = decl.Ident().size();
			for(int j = 0; j < num; j++) {
				target.add(new VarDeclWrapper(decl, j));
			}
		}
	}
	
	@Override
	public Object getParent(Object element) {
		if(element instanceof Tree) {
			return ((Tree)element).getParent();
		}
		return null;
	}
	
	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof ClassDeclContext) {
			return !((ClassDeclContext)element).varDecl().isEmpty();
			
		} else if(element instanceof MethodDeclContext) {
			return !((MethodDeclContext)element).varDecl().isEmpty();
			
		}
		return false;
	}
	
	/**
	 * Get the document provider for this model.
	 */
	public IDocumentProvider getDocumentProvider() {
		return mDocumentProvider;
	}
	
	/**
	 * Get the parser for the current input.
	 */
	public MicroJavaParser getParser() {
		return mParser;
	}
	
	/**
	 * Get the {@link RuleContext} for the program of this model.
	 */
	public ProgContext getRoot() {
		return mRoot;
	}
	
	/**
	 * Get the current document.
	 */
	public IDocument getDocument() {
		return mDocument;
	}
	
	/**
	 * Get the list of syntax and semantic errors encountered during parsing.
	 */
	public List<ParserError> getErrors() {
		return mErrors;
	}
	
	/**
	 * Get the tokens that are nearest to the specified offset:
	 * <ul>
	 * <li>If the offset is before the first or after the last token, the list contains the respective token.</li>
	 * <li>If the offset is inside a single token, or at the boundary of a token not adjacent to another, the
	 * list contains this token.</li>
	 * <li>If the offset is between two adjacent tokens, the list contains both tokens.</li>
	 * <li>Otherwise, the list contains the tokens immediately before and after the offset.</li>
	 * </ul>
	 * Note that the list may be empty if the parser was not able to parse the file successfully. This can be
	 * determined by checking whether {@link #getRoot()} returns a non-{@code null} value.
	 * 
	 * @param offset The 0-based offset in the document
	 * @return A list of {@link TerminalNode}s that are nearest to the offset (sorted by position)
	 */
	public List<TerminalNode> getTokensForOffset(final int offset) {
		List<TerminalNode> ret = new ArrayList<>(2);
		if(mRoot == null) {
			return ret;
		}
		final int maxIdx = mTokens.length - 1;
		
		// Corner cases: offset is before first or after last token
		if(offset < mTokens[0].getSymbol().getStartIndex()) {
			ret.add(mTokens[0]);
			return ret;
		} else if(mTokens[maxIdx].getSymbol().getStopIndex() + 1 < offset) {
			ret.add(mTokens[maxIdx]);
			return ret;
		}
		
		// Do a binary search (token positions are increasing)
		int low = 0;
		int high = maxIdx;
		while(low <= high) {
			final int mid = (low + high) >>> 1;
			final TerminalNode t = mTokens[mid];
			final int start = t.getSymbol().getStartIndex();
			final int stop = t.getSymbol().getStopIndex();
			
			if(start > offset) {
				high = mid - 1;
				
			} else if(stop + 1 < offset) {
				low = mid + 1;
				
			} else if(start == offset) {
				if(mid > 0 && mTokens[mid - 1].getSymbol().getStopIndex() + 1 == offset) {
					// Adjacent tokens, add both
					ret.add(mTokens[mid - 1]);
				}
				ret.add(t);
				return ret;
				
			} else if(stop + 1 == offset) {
				ret.add(t);
				if(mid < maxIdx && mTokens[mid + 1].getSymbol().getStartIndex() == offset) {
					// Adjacent tokens, add both
					ret.add(mTokens[mid + 1]);
				}
				return ret;
				
			} else {
				assert start < offset && stop >= offset;
				ret.add(t);
				return ret;
			}
		}
		
		// No tokens matched the offset exactly, return the nearest two
		assert low > 0 && low < maxIdx;
		assert mTokens[low].getSymbol().getStartIndex() > offset ||
				mTokens[low].getSymbol().getStopIndex() + 1 < offset;
		
		if(mTokens[low].getSymbol().getStartIndex() > offset) {
			ret.add(mTokens[low - 1]);
			ret.add(mTokens[low]);
		} else {
			ret.add(mTokens[low]);
			ret.add(mTokens[low - 1]);
		}
		
		return ret;
	}
	
	/**
	 * Get the source code range of the identifier for the specified object.
	 * <p>
	 * The object needs to be a {@link MethodDeclContext}, {@link ClassDeclContext}, {@link VarDeclWrapper},
	 * {@link ClassDeclContext} or {@link ProgContext}.
	 * 
	 * @param sel The selected object
	 * @return The identifier range, or {@code null}
	 */
	public static SourceRegion getIdentRange(Object sel) {
		TerminalNode ident = null;
		if(sel instanceof MethodDeclContext) {
			ident = ((MethodDeclContext)sel).Ident();
			
		} else if(sel instanceof ClassDeclContext) {
			ident = ((ClassDeclContext)sel).Ident();
			
		} else if(sel instanceof VarDeclWrapper) {
			ident = ((VarDeclWrapper)sel).getIdent();
			
		} else if(sel instanceof ConstDeclContext) {
			ident = ((ConstDeclContext)sel).Ident();
			
		} else if(sel instanceof ProgContext) {
			ident = ((ProgContext)sel).Ident();
			
		}
		
		if(ident != null && ident.getSymbol() != null) {
			Token t = ident.getSymbol();
			return new SourceRegion(t.getStartIndex(), t.getText().length());
		}
		return null;
	}
	
	/**
	 * Get the source code range of the selected element's parent.
	 * <p>
	 * A parent in this context is an element in the outline view like a class or method definition, variable
	 * declaration or a program.
	 * 
	 * @param sel The selected object
	 * @return The parent range, or {@code null}
	 * @see #getContainer(ParseTree)
	 */
	public static SourceRegion getParentRange(Object sel) {
		ParseTree parent = null;
		if(sel instanceof ParseTree) {
			parent = MJFileModel.getContainer((ParseTree)sel);
		} else if(sel instanceof VarDeclWrapper) {
			parent = MJFileModel.getContainer(((VarDeclWrapper)sel).getContext());
		}
		return getSourceRange(parent);
	}
	
	/**
	 * Get the source code range of the specified parse tree node.
	 * 
	 * @param node A node in the parse tree
	 * @return The source code range, or {@code null}
	 */
	public static SourceRegion getSourceRange(ParseTree node) {
		if(node != null) {
			TerminalNode start = getLeftLeaf(node);
			if(start == null) {
				return null;
			}
			// If start is not null, stop won't be either
			TerminalNode stop = getRightLeaf(node);
			int startIndex = start.getSymbol().getStartIndex();
			int stopIndex = stop.getSymbol().getStopIndex();
			return new SourceRegion(startIndex, stopIndex - startIndex + 1);
		}
		return null;
	}
	
	/**
	 * Gets the leftmost terminal node in the specified tree.
	 * 
	 * @param tree The tree to search
	 * @return The leftmost terminal node, or {@code null} if there are none
	 */
	private static TerminalNode getLeftLeaf(ParseTree tree) {
		Deque<ParseTree> stack = new LinkedList<>();
		stack.addLast(tree);
		while(!stack.isEmpty()) {
			ParseTree next = stack.removeLast();
			if(next instanceof TerminalNode) {
				if(!(next instanceof ErrorNode)) {
					return (TerminalNode)next;
				}
			}
			
			for(int j = next.getChildCount() - 1; j >= 0; j--) {
				stack.addLast(next.getChild(j));
			}
		}
		return null;
	}
	
	/**
	 * Gets the rightmost terminal node in the specified tree.
	 * 
	 * @param tree The tree to search
	 * @return The rightmost terminal node, or {@code null} if there are none
	 */
	private static TerminalNode getRightLeaf(ParseTree tree) {
		Deque<ParseTree> stack = new LinkedList<>();
		stack.addLast(tree);
		while(!stack.isEmpty()) {
			ParseTree next = stack.removeLast();
			if(next instanceof TerminalNode) {
				if(!(next instanceof ErrorNode)) {
					return (TerminalNode)next;
				}
			}
			
			final int childCount = next.getChildCount();
			for(int j = 0; j < childCount; j++) {
				stack.addLast(next.getChild(j));
			}
		}
		return null;
	}
	
	/**
	 * Collects all {@link TerminalNode}s in the specified tree, except error nodes.
	 * 
	 * @param tree The tree to collect terminal nodes from
	 * @return A list of terminal nodes, from left to right (that is, in the same order the appear in the
	 *         source code)
	 */
	private static List<TerminalNode> collectTerminalNodes(ParseTree tree) {
		Deque<ParseTree> stack = new LinkedList<>();
		List<TerminalNode> ret = new ArrayList<>();
		stack.addLast(tree);
		while(!stack.isEmpty()) {
			ParseTree next = stack.removeLast();
			if(next instanceof TerminalNode) {
				// It is assumed that Terminal nodes don't have children
				if(!(next instanceof ErrorNode)) {
					ret.add((TerminalNode)next);
				}
			} else {
				for(int j = next.getChildCount() - 1; j >= 0; j--) {
					stack.addLast(next.getChild(j));
				}
			}
		}
		return ret;
	}
	
	/**
	 * Get the container of the specified element.
	 * <p>
	 * A container is an element like a class or method definition, variable declaration or a program.
	 * 
	 * @param p The element
	 * @return The container, or {@code null} if none was found
	 */
	public static ParseTree getContainer(ParseTree p) {
		ParseTree ret = p;
		while(!(ret instanceof MethodDeclContext ||
				ret instanceof ClassDeclContext ||
				ret instanceof VarDeclContext ||
				ret instanceof ConstDeclContext ||
				ret instanceof ProgContext) && ret != null) {
			ret = ret.getParent();
		}
		return ret;
	}
}
