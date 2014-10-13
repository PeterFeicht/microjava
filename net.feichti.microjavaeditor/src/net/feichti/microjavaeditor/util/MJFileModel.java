package net.feichti.microjavaeditor.util;

import java.util.ArrayList;
import java.util.List;

import net.feichti.microjavaeditor.antlr4.MicroJavaLexer;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ClassDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.MethodDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ProgContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.Tree;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class MJFileModel implements ITreeContentProvider
{
	private final IDocumentProvider mDocumentProvider;

	public MJFileModel(IDocumentProvider documentProvider) {
		mDocumentProvider = documentProvider;
	}

	protected static final String ELEMENTS = "__microjava_elements";
	protected IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(ELEMENTS);
	protected MicroJavaParser mParser = null;
	protected ProgContext mRoot = null;
	
	private void parse(IDocument doc) {
		try {
			MicroJavaLexer lex = new MicroJavaLexer(new ANTLRInputStream(doc.get()));
			mParser = new MicroJavaParser(new BufferedTokenStream(lex));
			mRoot = mParser.prog();
		} catch(RecognitionException ex) {
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
			IDocument doc = mDocumentProvider.getDocument(oldInput);
			if(doc != null) {
				try {
					doc.removePositionCategory(ELEMENTS);
				} catch(BadPositionCategoryException ex) {
					
				}
				doc.removePositionUpdater(mPositionUpdater);
			}
		}
		
		mRoot = null;
		mParser = null;
		
		if(newInput != null) {
			IDocument doc = mDocumentProvider.getDocument(newInput);
			if(doc != null) {
				doc.addPositionCategory(ELEMENTS);
				doc.addPositionUpdater(mPositionUpdater);
				parse(doc);
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
	
	@Override
	public Object getParent(Object element) {
		if(element instanceof Tree) {
			return ((Tree)element).getParent();
		}
		return null;
	}
	
	@Override
	public boolean hasChildren(Object element) {
		// TODO optimize
		return getChildren(element).length > 0;
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
}