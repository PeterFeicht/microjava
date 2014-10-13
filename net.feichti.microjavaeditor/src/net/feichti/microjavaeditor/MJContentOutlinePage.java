package net.feichti.microjavaeditor;

import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ClassDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ConstDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.MethodDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ProgContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;
import net.feichti.microjavaeditor.util.MJFileModel;
import net.feichti.microjavaeditor.util.MJLabelProvider;
import net.feichti.microjavaeditor.util.VarDeclWrapper;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class MJContentOutlinePage extends ContentOutlinePage
{
	protected Object mInput;
	protected IDocumentProvider mDocumentProvider;
	protected ITextEditor mTextEditor;
	protected MJFileModel mFileModel;
	
	/**
	 * Creates a content outline page using the given provider and the given editor.
	 *
	 * @param provider the document provider
	 * @param editor the editor
	 */
	public MJContentOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		mDocumentProvider = provider;
		mTextEditor = editor;
		mFileModel = new MJFileModel(mDocumentProvider);
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(mFileModel);
		viewer.setLabelProvider(MJLabelProvider.create());
		viewer.addSelectionChangedListener(this);
		
		if(mInput != null) {
			viewer.setInput(mInput);
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		
		ITreeSelection selection = (ITreeSelection)event.getSelection();
		if(!selection.isEmpty()) {
			Object sel = selection.getFirstElement();
			Region r = getIdentRange(sel);
			if(r != null) {
				mTextEditor.selectAndReveal(r.getOffset(), r.getLength());
			}
			r = getParentRange(sel);
			if(r != null) {
				mTextEditor.setHighlightRange(r.getOffset(), r.getLength(), false);
			}
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
				viewer.expandToLevel(1);
				control.setRedraw(true);
			}
		}
	}
	
	/**
	 * Get the source code range of the identifier for the specified object.
	 * <p>
	 * The object needs to be a {@link MethodDeclContext}, {@link ClassDeclContext}, {@link VarDeclWrapper},
	 * {@link VarDeclContext}, {@link ClassDeclContext} or {@link ProgContext}.
	 * 
	 * @param sel The selected object
	 * @return The identifier range, or {@code null}
	 */
	private static Region getIdentRange(Object sel) {
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
	private static Region getParentRange(Object sel) {
		ParseTree parent = null;
		if(sel instanceof ParseTree) {
			parent = getContainer((ParseTree)sel);
		} else if(sel instanceof VarDeclWrapper) {
			parent = getContainer(((VarDeclWrapper)sel).getContext());
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
	private static ParseTree getContainer(ParseTree p) {
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
