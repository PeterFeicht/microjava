package net.feichti.microjavaeditor;

import net.feichti.microjavaeditor.microjava.MJAutoIndentStrategy;
import net.feichti.microjavaeditor.microjava.MJCompletionProcessor;
import net.feichti.microjavaeditor.microjava.MJDoubleClickSelector;
import net.feichti.microjavaeditor.util.MJColorManager;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class MJSourceViewerConfiguration extends SourceViewerConfiguration
{
	static class SingleTokenScanner extends BufferedRuleBasedScanner
	{
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	}
	
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new MJAnnotationHover();
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		IAutoEditStrategy s = (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)
				? new MJAutoIndentStrategy()
				: new DefaultIndentLineAutoEditStrategy());
		return new IAutoEditStrategy[] { s };
	}
	
	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return MicroJavaEditorPlugin.MICROJAVA_PARTITIONING;
	}
	
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, MJPartitionScanner.MICROJAVA_COMMENT };
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new MJCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(0);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		
		return assistant;
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new MJDoubleClickSelector();
	}
	
	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "\t", "    " };
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		MJColorManager colorManager = MicroJavaEditorPlugin.getDefault().getColorManager();
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(MicroJavaEditorPlugin.getDefault().getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(
				colorManager.getColor(MJColorManager.COMMENT))));
		reconciler.setDamager(dr, MJPartitionScanner.MICROJAVA_COMMENT);
		reconciler.setRepairer(dr, MJPartitionScanner.MICROJAVA_COMMENT);
		
		return reconciler;
	}
	
	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 4;
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new MJTextHover();
	}
}
