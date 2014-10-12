package net.feichti.microjavaeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class MJEditor extends TextEditor
{
	public final static String MATCHING_BRACKETS = "matchingBrackets";
	public final static String HIGHLIGHT_BRACKET_AT_CARET_LOCATION = "highlightBracketAtCaretLocation";
	public final static String ENCLOSING_BRACKETS = "enclosingBrackets";
	public final static String MATCHING_BRACKETS_COLOR = "matchingBracketsColor";
	
	private MJContentOutlinePage mOutlinePage;
	private ICharacterPairMatcher mBracketMatcher;
	
	public MJEditor() {
		mBracketMatcher = new DefaultCharacterPairMatcher(new char[] { '(', ')', '{', '}', '[', ']' });
	}
	
	@Override
	public void dispose() {
		if(mOutlinePage != null) {
			mOutlinePage.dispose();
		}
		super.dispose();
	}
	
	@Override
	public void doRevertToSaved() {
		super.doRevertToSaved();
		if(mOutlinePage != null) {
			mOutlinePage.update();
		}
	}
	
	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		if(mOutlinePage != null) {
			mOutlinePage.update();
		}
	}
	
	@Override
	public void doSaveAs() {
		super.doSaveAs();
		if(mOutlinePage != null) {
			mOutlinePage.update();
		}
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if(mOutlinePage != null) {
			mOutlinePage.setInput(input);
		}
	}
	
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		
		addAction(menu, "ContentAssistProposal");
		addAction(menu, "ContentAssistTip");
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if(IContentOutlinePage.class.equals(adapter)) {
			if(mOutlinePage == null) {
				mOutlinePage = new MJContentOutlinePage(getDocumentProvider(), this);
				if(getEditorInput() != null) {
					mOutlinePage.setInput(getEditorInput());
				}
			}
			return mOutlinePage;
		}
		
		return super.getAdapter(adapter);
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new MJSourceViewerConfiguration());
	}
	
	@Override
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer = getSourceViewer();
		if(viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 e5 = (ITextViewerExtension5)viewer;
			e5.exposeModelRange(new Region(offset, length));
		}
	}
	
	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		support.setCharacterPairMatcher(mBracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR,
				HIGHLIGHT_BRACKET_AT_CARET_LOCATION, ENCLOSING_BRACKETS);
		
		setPreferenceDefaults();
		
		super.configureSourceViewerDecorationSupport(support);
	}
	
	/**
	 * Set the default values for bracket matching preferences if necessary.
	 */
	private void setPreferenceDefaults() {
		IPreferenceStore pref = getPreferenceStore();
		if(!pref.contains(MATCHING_BRACKETS)) {
			pref.setValue(MATCHING_BRACKETS, true);
		}
		if(!pref.contains(MATCHING_BRACKETS_COLOR)) {
			pref.setValue(MATCHING_BRACKETS_COLOR, "255,0,0");
		}
		if(!pref.contains(HIGHLIGHT_BRACKET_AT_CARET_LOCATION)) {
			pref.setValue(HIGHLIGHT_BRACKET_AT_CARET_LOCATION, true);
		}
		if(!pref.contains(ENCLOSING_BRACKETS)) {
			pref.setValue(ENCLOSING_BRACKETS, false);
		}
	}
}
