package net.feichti.microjavaeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
	/**
	 * A segment element.
	 */
	protected static class Segment
	{
		public String name;
		public Position position;
		
		public Segment(String name, Position position) {
			this.name = name;
			this.position = position;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Divides the editor's document into ten segments and provides elements for them.
	 */
	protected class ContentProvider implements ITreeContentProvider
	{
		protected final static String SEGMENTS = "__java_segments";
		protected IPositionUpdater mPositionUpdater = new DefaultPositionUpdater(SEGMENTS);
		protected List<Segment> mContent = new ArrayList<Segment>(10);
		
		protected void parse(IDocument document) {
			int lines = document.getNumberOfLines();
			int increment = Math.max(Math.round(lines / 10), 10);
			
			for(int line = 0; line < lines; line += increment) {
				int length = increment;
				if(line + increment > lines) {
					length = lines - line;
				}
				
				try {
					int offset = document.getLineOffset(line);
					int end = document.getLineOffset(line + length);
					length = end - offset;
					Position p = new Position(offset, length);
					document.addPosition(SEGMENTS, p);
					mContent.add(new Segment(MJEditorMessages.format("OutlinePage.segment.title_pattern", offset), p));
				} catch(BadPositionCategoryException x) {
					
				} catch(BadLocationException x) {
					
				}
			}
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if(oldInput != null) {
				IDocument document = mDocumentProvider.getDocument(oldInput);
				if(document != null) {
					try {
						document.removePositionCategory(SEGMENTS);
					} catch(BadPositionCategoryException x) {
						
					}
					document.removePositionUpdater(mPositionUpdater);
				}
			}
			
			mContent.clear();
			
			if(newInput != null) {
				IDocument document = mDocumentProvider.getDocument(newInput);
				if(document != null) {
					document.addPositionCategory(SEGMENTS);
					document.addPositionUpdater(mPositionUpdater);
					
					parse(document);
				}
			}
		}
		
		@Override
		public void dispose() {
			if(mContent != null) {
				mContent.clear();
				mContent = null;
			}
		}
		
		@Override
		public Object[] getElements(Object element) {
			return mContent.toArray();
		}
		
		@Override
		public boolean hasChildren(Object element) {
			return element == mInput;
		}
		
		@Override
		public Object getParent(Object element) {
			if(element instanceof Segment) {
				return mInput;
			}
			return null;
		}
		
		@Override
		public Object[] getChildren(Object element) {
			if(element == mInput) {
				return mContent.toArray();
			}
			return new Object[0];
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
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
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
			Segment segment = (Segment)((IStructuredSelection)selection).getFirstElement();
			int start = segment.position.getOffset();
			int length = segment.position.getLength();
			try {
				mTextEditor.setHighlightRange(start, length, true);
			} catch(IllegalArgumentException x) {
				mTextEditor.resetHighlightRange();
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
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
}
