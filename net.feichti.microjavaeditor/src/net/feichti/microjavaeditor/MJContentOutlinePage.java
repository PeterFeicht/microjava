package net.feichti.microjavaeditor;

import net.feichti.microjavaeditor.util.MJFileModel;
import net.feichti.microjavaeditor.util.MJLabelProvider;

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
	protected MJEditor mEditor;
	protected MJFileModel mFileModel;
	
	/**
	 * Creates a content outline page using the given provider and the given editor.
	 *
	 * @param provider the document provider
	 * @param editor the editor
	 */
	public MJContentOutlinePage(IDocumentProvider provider, MJEditor editor) {
		mDocumentProvider = provider;
		mEditor = editor;
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
			Region ident = MJFileModel.getIdentRange(sel);
			if(ident != null) {
				mEditor.selectAndReveal(ident.getOffset(), ident.getLength());
			}
			mEditor.updateHighlight();
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
	 * Get the document provider for this outline page.
	 */
	public IDocumentProvider getDocumentProvider() {
		return mDocumentProvider;
	}
	
	/**
	 * Get the text editor for this outline page.
	 */
	public ITextEditor getTextEditor() {
		return mEditor;
	}
	
	/**
	 * Get the file model associated with this outline page.
	 */
	public MJFileModel getFileModel() {
		return mFileModel;
	}
}
