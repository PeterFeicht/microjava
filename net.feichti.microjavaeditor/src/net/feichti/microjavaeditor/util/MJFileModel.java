package net.feichti.microjavaeditor.util;

import java.util.ArrayList;
import java.util.List;

import net.feichti.microjavaeditor.antlr4.MicroJavaLexer;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ClassDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ConstDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.MethodDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ProgContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
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

public class MJFileModel implements ITreeContentProvider
{
	public static final String ELEMENTS = "__microjava_elements";
	private final IDocumentProvider mDocumentProvider;
	private IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(ELEMENTS);
	private MicroJavaParser mParser = null;
	private ProgContext mRoot = null;
	private IDocument mDocument = null;
	
	public MJFileModel(IDocumentProvider documentProvider) {
		mDocumentProvider = documentProvider;
	}

	private void parse(IDocument doc) {
		try {
			MicroJavaLexer lex = new MicroJavaLexer(new ANTLRInputStream(doc.get()));
			mParser = new MicroJavaParser(new BufferedTokenStream(lex));
			mRoot = mParser.prog();
			
			System.out.println("parse finished");
		} catch(RecognitionException ex) {
			System.out.println("parse failed:");
			ex.printStackTrace();
			mRoot = null;
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
	 * Get the source code range of the identifier for the specified object.
	 * <p>
	 * The object needs to be a {@link MethodDeclContext}, {@link ClassDeclContext}, {@link VarDeclWrapper},
	 * {@link ClassDeclContext} or {@link ProgContext}.
	 * 
	 * @param sel The selected object
	 * @return The identifier range, or {@code null}
	 */
	public static Region getIdentRange(Object sel) {
		Token ident = null;
		if(sel instanceof MethodDeclContext) {
			ident = ((MethodDeclContext)sel).Ident().getSymbol();
			
		} else if(sel instanceof ClassDeclContext) {
			ident = ((ClassDeclContext)sel).Ident().getSymbol();
			
		} else if(sel instanceof VarDeclWrapper) {
			ident = ((VarDeclWrapper)sel).getIdent().getSymbol();
			
		} else if(sel instanceof ConstDeclContext) {
			ident = ((ConstDeclContext)sel).Ident().getSymbol();
			
		} else if(sel instanceof ProgContext) {
			ident = ((ProgContext)sel).Ident().getSymbol();
			
		}
		
		if(ident != null) {
			return new Region(ident.getStartIndex(), ident.getText().length());
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
			ParseTree start = parent.getChild(0);
			ParseTree stop = parent.getChild(parent.getChildCount() - 1);
			while(!(start instanceof TerminalNode)) {
				start = start.getChild(0);
			}
			while(!(stop instanceof TerminalNode)) {
				stop = stop.getChild(stop.getChildCount() - 1);
			}
			
			int startIndex = ((TerminalNode)start).getSymbol().getStartIndex();
			int stopIndex = ((TerminalNode)stop).getSymbol().getStopIndex();
			return new Region(startIndex, stopIndex - startIndex);
		}
		return null;
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