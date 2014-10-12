package net.feichti.microjavaeditor;

import java.util.ArrayList;
import java.util.List;

import net.feichti.microjavaeditor.antlr4.MicroJavaLexer;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ClassDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.MethodDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ProgContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;
import net.feichti.microjavaeditor.util.MJLabelProvider;
import net.feichti.microjavaeditor.util.VarDeclWrapper;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.Tree;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class MJContentOutlinePage extends ContentOutlinePage
{
	protected class ChildrenProvider implements ITreeContentProvider
	{
		protected static final String ELEMENTS = "__microjava_elements";
		protected IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(ELEMENTS);
		protected ProgContext mRoot = null;
		
		protected void parse(IDocument doc) {
			try {
				MicroJavaLexer lex = new MicroJavaLexer(new ANTLRInputStream(doc.get()));
				MicroJavaParser parser = new MicroJavaParser(new BufferedTokenStream(lex));
				mRoot = parser.prog();
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
		
		private void addVarDecls(List<Object> target, List<VarDeclContext> varDecls) {
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
			// TODO optimize
			return getChildren(element).length > 0;
		}
	}
	
	protected Object mInput;
	protected IDocumentProvider mDocumentProvider;
	protected ITextEditor mTextEditor;
	
	/**
	 * Creates a content outline page using the given provider and the given editor.
	 *
	 * @param provider the document provider
	 * @param editor the editor
	 */
	public MJContentOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		mDocumentProvider = provider;
		mTextEditor = editor;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new ChildrenProvider());
		viewer.setLabelProvider(MJLabelProvider.create());
		viewer.addSelectionChangedListener(this);
		
		if(mInput != null) {
			viewer.setInput(mInput);
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		
		ISelection selection = event.getSelection();
		if(selection.isEmpty()) {
			mTextEditor.resetHighlightRange();
		} else {
			// TODO handle selection event
		}
	}
	
	/**
	 * Sets the input of the outline page
	 *
	 * @param input the input of this outline page
	 */
	public void setInput(Object input) {
		mInput = input;
		update();
	}
	
	/**
	 * Updates the outline page.
	 */
	public void update() {
		TreeViewer viewer = getTreeViewer();
		
		if(viewer != null) {
			Control control = viewer.getControl();
			if(control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(mInput);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
}
