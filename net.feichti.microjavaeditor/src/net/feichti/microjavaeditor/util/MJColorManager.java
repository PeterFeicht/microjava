package net.feichti.microjavaeditor.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class MJColorManager
{
	public static final RGB COMMENT = new RGB(128, 128, 0);
	public static final RGB KEYWORD = new RGB(127, 0, 85);
	public static final RGB IDENTIFIER = new RGB(0, 128, 255);
	public static final RGB CONSTANT = new RGB(0, 128, 255);
	public static final RGB TYPE = new RGB(0, 128, 255);
	public static final RGB CHAR_CONST = new RGB(0, 128, 0);
	public static final RGB NUMBER = new RGB(255, 0, 0);
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	public static final RGB OPERATOR = new RGB(128, 0, 255);
	public static final RGB BRACKET = new RGB(0, 0, 0);
	
	protected Map<RGB, Color> mColorTable = new HashMap<>(10);
	
	public void dispose() {
		Iterator<Color> e = mColorTable.values().iterator();
		while(e.hasNext()) {
			e.next().dispose();
		}
	}
	
	public Color getColor(RGB rgb) {
		Color color = mColorTable.get(rgb);
		if(color == null) {
			color = new Color(Display.getCurrent(), rgb);
			mColorTable.put(rgb, color);
		}
		return color;
	}
}
