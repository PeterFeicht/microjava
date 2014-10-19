package net.feichti.microjavaeditor;

import java.util.List;

import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;
import net.feichti.microjavaeditor.microjava.MJFileModel;
import net.feichti.microjavaeditor.microjava.MJFileModel.VariableKind;
import net.feichti.microjavaeditor.util.SourceRegion;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class MJEditor extends TextEditor implements ISelectionChangedListener
{
	/**
	 * The ID of this editor.
	 */
	public static final String ID = "net.feichti.microjavaeditor.MJEditor";
	
	// Preference keys
	public final static String MATCHING_BRACKETS = "matchingBrackets";
	public final static String HIGHLIGHT_BRACKET_AT_CARET_LOCATION = "highlightBracketAtCaretLocation";
	public final static String ENCLOSING_BRACKETS = "enclosingBrackets";
	public final static String MATCHING_BRACKETS_COLOR = "matchingBracketsColor";
	
	private MJContentOutlinePage mOutlinePage;
	private ICharacterPairMatcher mBracketMatcher;
	
	// TODO semantic highlighting (StyleRange)
	
	public MJEditor() {
		mBracketMatcher = new DefaultCharacterPairMatcher(new char[] { '(', ')', '{', '}', '[', ']' });
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		ISelectionProvider selectionProvider = getSelectionProvider();
		if(selectionProvider instanceof IPostSelectionProvider) {
			((IPostSelectionProvider)selectionProvider).addPostSelectionChangedListener(this);
		} else {
			selectionProvider.addSelectionChangedListener(this);
		}
	}
	
	@Override
	public void dispose() {
		if(mOutlinePage != null) {
			mOutlinePage.dispose();
		}
		ISelectionProvider selectionProvider = getSelectionProvider();
		if(selectionProvider instanceof IPostSelectionProvider) {
			((IPostSelectionProvider)selectionProvider).removePostSelectionChangedListener(this);
		} else {
			selectionProvider.removeSelectionChangedListener(this);
		}
		super.dispose();
	}
	
	@Override
	public void doRevertToSaved() {
		super.doRevertToSaved();
		if(mOutlinePage != null) {
			mOutlinePage.update();
			updateHighlight();
		}
	}
	
	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		if(mOutlinePage != null) {
			mOutlinePage.update();
			updateHighlight();
		}
	}
	
	@Override
	public void doSaveAs() {
		super.doSaveAs();
		if(mOutlinePage != null) {
			mOutlinePage.update();
			updateHighlight();
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
		} else if(MJFileModel.class.equals(adapter)) {
			if(mOutlinePage == null) {
				getAdapter(IContentOutlinePage.class);
			}
			return mOutlinePage.getFileModel();
		}
		
		return super.getAdapter(adapter);
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new MJSourceViewerConfiguration(this));
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
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateHighlight();
	}
	
	/**
	 * Update the highlighting range according to the current selection. Requires that the outline page be
	 * set.
	 */
	public void updateHighlight() {
		updateHighlight(false);
	}
	
	/**
	 * Update the highlighting range according to the current selection. Requires that the outline page be
	 * set.
	 * 
	 * @param reveal Whether to reveal the new highlight range as well
	 */
	public void updateHighlight(boolean reveal) {
		final ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
		List<TerminalNode> tokens = mOutlinePage.getFileModel().getTokensForOffset(sel.getOffset());
		// TODO consider selection end to decide on highlight range
		
		ParseTree parent;
		if(tokens.isEmpty()) {
			return;
		} else if(tokens.size() == 1) {
			// Inside a token, highlight the container
			parent = MJFileModel.getContainer(tokens.get(0));
		} else {
			// Between two tokens, decide which container to highlight
			ParseTree p1 = MJFileModel.getContainer(tokens.get(0));
			ParseTree p2 = MJFileModel.getContainer(tokens.get(1));
			if(p1 == p2) {
				parent = p1;
			} else {
				SourceRegion r1 = MJFileModel.getSourceRange(p1);
				SourceRegion r2 = MJFileModel.getSourceRange(p2);
				if(r1.contains(r2)) {
					parent = p1;
				} else if(r2.contains(r1)) {
					parent = p2;
				} else {
					// Disjoint regions, highlight parent of both
					parent = p1.getParent();
				}
			}
		}
		
		assert parent != null;
		if(parent instanceof VarDeclContext) {
			if(VariableKind.forDeclaration((VarDeclContext)parent) == VariableKind.LOCAL) {
				// Highlight method for local variables
				parent = parent.getParent();
			}
		}
		IRegion range = MJFileModel.getSourceRange(parent);
		assert range != null;
		setHighlightRange(range.getOffset(), range.getLength(), false);
		if(reveal) {
			getSourceViewer().revealRange(range.getOffset(), range.getLength());
		}
	}
}
