package net.feichti.microjavaeditor;

import java.util.Iterator;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;

public class MJTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2
{
	private MJEditor mEditor;
	private ISourceViewer mSourceViewer;
	
	public MJTextHover(MJEditor editor, ISourceViewer sourceViewer) {
		mSourceViewer = sourceViewer;
		mEditor = editor;
	}
	
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model = getAnnotationModel(mSourceViewer);
		if(model == null) {
			return null;
		}
		
		Iterator<Annotation> e = model.getAnnotationIterator();
		while(e.hasNext()) {
			Annotation a = e.next();
			if(!isIncluded(a)) {
				continue;
			}
			
			Position p = model.getPosition(a);
			if(p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
				String msg = a.getText();
				if(msg != null && msg.trim().length() > 0) {
					return msg;
				}
			}
		}
		
		return null;
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		return getHoverInfo(textViewer, hoverRegion);
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}
	
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}
	
	@SuppressWarnings("static-method")
	protected boolean isIncluded(Annotation annotation) {
		if(annotation.getType() != null) {
			return !annotation.getType().startsWith("org.eclipse.ui.workbench.texteditor.quickdiff");
		}
		return true;
	}
	
	private static IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
		if(viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 extension = (ISourceViewerExtension2)viewer;
			return extension.getVisualAnnotationModel();
		}
		return viewer.getAnnotationModel();
	}
}
