package net.feichti.microjavaeditor;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;

public class MJTextHover implements ITextHover
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
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return findWord(textViewer.getDocument(), offset);
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
	
	private static IRegion findWord(IDocument document, int offset) {
		int start = -2;
		int end = -1;
		
		try {
			int pos = offset;
			char c;
			
			while(pos >= 0) {
				c = document.getChar(pos);
				if(!Character.isUnicodeIdentifierPart(c)) {
					break;
				}
				--pos;
			}
			start = pos;
			
			pos = offset;
			int length = document.getLength();
			
			while(pos < length) {
				c = document.getChar(pos);
				if(!Character.isUnicodeIdentifierPart(c)) {
					break;
				}
				++pos;
			}
			end = pos;
		} catch(BadLocationException x) {
			
		}
		
		if(start >= -1 && end > -1) {
			if(start == offset && end == offset) {
				return new Region(offset, 0);
			} else if(start == offset) {
				return new Region(start, end - start);
			} else {
				return new Region(start + 1, end - start - 1);
			}
		}
		
		return null;
	}
}
