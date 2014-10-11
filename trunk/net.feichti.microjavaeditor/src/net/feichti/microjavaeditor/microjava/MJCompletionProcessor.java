package net.feichti.microjavaeditor.microjava;

import net.feichti.microjavaeditor.MJEditorMessages;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Example MicroJava completion processor.
 */
public class MJCompletionProcessor implements IContentAssistProcessor
{
	/**
	 * Simple content assist tip closer. The tip is valid in a range of 5 characters around its popup
	 * location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter
	{
		protected int mInstallOffset;
		
		@Override
		public boolean isContextInformationValid(int offset) {
			return Math.abs(mInstallOffset - offset) < 5;
		}
		
		@Override
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			mInstallOffset = offset;
		}
		
		@Override
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}
	
	protected final static String[] PROPOSALS = { "break", "case", "char", "class", "default", "else", "final", "if",
			"int", "new", "null", "return", "switch", "void" };
	
	protected IContextInformationValidator mValidator = new Validator();
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		ICompletionProposal[] result = new ICompletionProposal[PROPOSALS.length];
		for(int i = 0; i < PROPOSALS.length; i++) {
			IContextInformation info = new ContextInformation(PROPOSALS[i],
					MJEditorMessages.format("CompletionProcessor.Proposal.ContextInfo.pattern", PROPOSALS[i]));
			result[i] = new CompletionProposal(PROPOSALS[i], documentOffset, 0, PROPOSALS[i].length(), null,
					PROPOSALS[i], info,
					MJEditorMessages.format("CompletionProcessor.Proposal.hoverinfo.pattern", PROPOSALS[i]));
		}
		return result;
	}
	
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result = new IContextInformation[5];
		for(int i = 0; i < result.length; i++) {
			String context = MJEditorMessages.format("CompletionProcessor.ContextInfo.display.pattern",
					i, documentOffset);
			String info = MJEditorMessages.format("CompletionProcessor.ContextInfo.value.pattern",
					i, documentOffset - 5, documentOffset + 5);
			result[i] = new ContextInformation(context, info);
		}
		return result;
	}
	
	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.', '(' };
	}
	
	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '#' };
	}
	
	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return mValidator;
	}
	
	@Override
	public String getErrorMessage() {
		return null;
	}
}
