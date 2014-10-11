package net.feichti.microjavaeditor;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

public class MJActionContributor extends TextEditorActionContributor
{
	protected RetargetTextEditorAction mContentAssistProposal;
	protected RetargetTextEditorAction mContentAssistTip;
	
	public MJActionContributor() {
		mContentAssistProposal =
				new RetargetTextEditorAction(MJEditorMessages.getResourceBundle(), "ContentAssistProposal.");
		mContentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		mContentAssistTip = new RetargetTextEditorAction(MJEditorMessages.getResourceBundle(), "ContentAssistTip.");
		mContentAssistTip.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
	}
	
	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		
		IMenuManager menuManager = bars.getMenuManager();
		IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if(editMenu != null) {
			editMenu.add(new Separator());
			editMenu.add(mContentAssistProposal);
			editMenu.add(mContentAssistTip);
		}
	}
	
	private void doSetActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		
		ITextEditor editor = null;
		if(part instanceof ITextEditor) {
			editor = (ITextEditor)part;
		}
		
		mContentAssistProposal.setAction(getAction(editor, ITextEditorActionConstants.CONTENT_ASSIST));
		mContentAssistTip.setAction(getAction(editor, ITextEditorActionConstants.CONTENT_ASSIST_CONTEXT_INFORMATION));
	}
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}
	
	@Override
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}
}
