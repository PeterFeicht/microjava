package net.feichti.microjavaeditor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;

public class MJTextHover implements ITextHover
{
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if(hoverRegion != null) {
			try {
				if(hoverRegion.getLength() > -1) {
					return textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
				}
			} catch(BadLocationException x) {
				
			}
		}
		return MJEditorMessages.getString("JavaTextHover.emptySelection");
	}
	
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if(selection.x <= offset && offset <= selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		}
		return new Region(offset, 0);
	}
}
