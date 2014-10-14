package net.feichti.microjavaeditor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.feichti.microjavaeditor.MJContentOutlinePage;
import net.feichti.microjavaeditor.antlr4.MicroJavaLexer;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ClassDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ConstDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.MethodDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ProgContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
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
	public static final String ELEMENTS = "__microjava_elements";
	
	private final IPositionUpdater mPositionUpdater;
	private final IDocumentProvider mDocumentProvider;
	
	/** The parser used to parse the current document, kept even on error. */
	private MicroJavaParser mParser = null;
	/** The context on successful parse, {@code null} otherwise. */
	private ProgContext mRoot = null;
	/** The list of tokens for position search on successful parse, {@code null} otherwise. */
	private Token[] mTokens;
	/** The current document. */
	private IDocument mDocument = null;
	
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
	 */
	private void parse(IDocument doc) {
		try {
			MicroJavaLexer lex = new MicroJavaLexer(new ANTLRInputStream(doc.get()));
			CommonTokenStream tokens = new CommonTokenStream(lex);
			mParser = new MicroJavaParser(tokens);
			// TODO replace ConsoleErrorListener
			mRoot = mParser.prog();
			
			int numTokens = tokens.getNumberOfOnChannelTokens();
			if(numTokens > 1) {
				List<Token> tmp = new ArrayList<>(numTokens + 1);
				for(Token t : tokens.getTokens()) {
					if(t.getChannel() == Token.DEFAULT_CHANNEL) {
						tmp.add(t);
					}
				}
				mTokens = tmp.toArray(new Token[0]);
			} else {
				// We need two tokens for position search, a program with one token makes no sense anyway
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
	}
	
	@Override
	public void dispose() {
		mRoot = null;
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
		}
		
		mRoot = null;
		mParser = null;
		mDocument = null;
		mTokens = null;
		
		if(newInput != null) {
			mDocument = mDocumentProvider.getDocument(newInput);
			if(mDocument != null) {
				mDocument.addPositionCategory(ELEMENTS);
				mDocument.addPositionUpdater(mPositionUpdater);
				parse(mDocument);
			}
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
	 * Get the {@link Token}s that are nearest to the specified offset:
	 * <ul>
	 * <li>If the offset is inside a single token, then the list contains this token.</li>
	 * <li>If the offset is between two tokens, the list contains both tokens.</li>
	 * <li>If the offset is at the beginning of a token, the list contains this token and the one before it,
	 * if any.</li>
	 * <li>If the offset is at the end of a token, the list contains this token and the one after it, if any.</li>
	 * </ul>
	 * 
	 * @param offset The 0-based offset in the document
	 * @return A list of nodes that are nearest to the offset
	 */
	public List<Token> getTokensForOffset(final int offset) {
		List<Token> ret = new ArrayList<>(2);
		
		int idx = Arrays.binarySearch(mTokens, null, new Comparator<Token>() {
			@Override
			public int compare(Token o1, Token o2) {
				if(o1 != null) {
					if(o1.getStartIndex() > offset) {
						return 1;
					} else if(o1.getStopIndex() < offset) {
						return -1;
					}
				} else {
					if(o2.getStopIndex() < offset) {
						return 1;
					} else if(o2.getStartIndex() > offset) {
						return -1;
					}
				}
				return 0;
			}
		});
		
		if(idx <= 0) {
			ret.add(mTokens[0]);
			if(mTokens[0].getStopIndex() <= offset) {
				ret.add(mTokens[1]);
			}
		}
		if(idx == mTokens.length) {
			ret.add(mTokens[mTokens.length - 1]);
		} else {
			Token found = mTokens[idx];
			ret.add(found);
			if(found.getStartIndex() == offset) {
				ret.add(mTokens[idx - 1]);
			} else if(found.getStopIndex() == offset && (idx + 1 < mTokens.length)) {
				ret.add(mTokens[idx + 1]);
			}
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
	public static Region getIdentRange(Object sel) {
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
			return new Region(t.getStartIndex(), t.getText().length());
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
	public static Region getParentRange(Object sel) {
		ParseTree parent = null;
		if(sel instanceof ParseTree) {
			parent = MJFileModel.getContainer((ParseTree)sel);
		} else if(sel instanceof VarDeclWrapper) {
			parent = MJFileModel.getContainer(((VarDeclWrapper)sel).getContext());
		}
		
		if(parent != null) {
			int startIndex = getFirstLeaf(parent).getSymbol().getStartIndex();
			int stopIndex = getLastLeaf(parent).getSymbol().getStopIndex();
			return new Region(startIndex, stopIndex - startIndex);
		}
		return null;
	}
	
	/**
	 * Get the first leaf node for the specified token, assuming that leaf nodes are always
	 * {@link TerminalNode}s.
	 * 
	 * @param parent The parent
	 * @return The first leaf node
	 */
	private static TerminalNode getFirstLeaf(ParseTree parent) {
		ParseTree start = parent.getChild(0);
		while(!(start instanceof TerminalNode)) {
			start = start.getChild(0);
		}
		return (TerminalNode)start;
	}
	
	/**
	 * Get the last leaf node for the specified token, assuming that leaf nodes are always
	 * {@link TerminalNode}s.
	 * 
	 * @param parent The parent
	 * @return The last leaf node
	 */
	private static TerminalNode getLastLeaf(ParseTree parent) {
		ParseTree stop = parent.getChild(parent.getChildCount() - 1);
		while(!(stop instanceof TerminalNode) && stop != null) {
			stop = stop.getChild(stop.getChildCount() - 1);
		}
		return (TerminalNode)stop;
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
