package net.feichti.microjavaeditor.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class MJColorManager
{
	public static class KeywordStyle
	{
		public final RGB color;
		public final boolean bold;
		public final boolean italic;
		public final boolean underline;
		
		public KeywordStyle(RGB color) {
			this(color, false, false, false);
		}
		
		public KeywordStyle(RGB color, boolean bold, boolean italic) {
			this(color, bold, italic, false);
		}
		
		public KeywordStyle(RGB color, boolean bold, boolean italic, boolean underline) {
			this.color = color;
			this.bold = bold;
			this.italic = italic;
			this.underline = underline;
		}
		
		public IToken getStyleToken(MJColorManager colorManager) {
			int style = SWT.NORMAL;
			style |= (bold ? SWT.BOLD : 0);
			style |= (italic ? SWT.ITALIC : 0);
			style |= (underline ? SWT.UNDERLINE_SINGLE : 0);
			
			TextAttribute attr = new TextAttribute(colorManager.getColor(color), null, style);
			return new Token(attr);
		}
	}
	
	public static final RGB COMMENT = new RGB(128, 128, 0);
	public static final RGB TASK_TAG = new RGB(127, 159, 191);
	public static final RGB KEYWORD = new RGB(127, 0, 85);
	public static final RGB IDENTIFIER = new RGB(0, 128, 255);
	public static final RGB CONSTANT = new RGB(0, 128, 255);
	public static final RGB TYPE = new RGB(0, 128, 255);
	public static final RGB CLASS = new RGB(0, 0, 0);
	public static final RGB CHAR_CONST = new RGB(0, 128, 0);
	public static final RGB NUMBER = new RGB(255, 0, 0);
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	public static final RGB OPERATOR = new RGB(128, 0, 255);
	public static final RGB BRACKET = new RGB(0, 0, 0);
	public static final RGB MAIN_METHOD = new RGB(0, 0, 0);
	
	public static final KeywordStyle COMMENT_STYLE = new KeywordStyle(COMMENT);
	public static final KeywordStyle TASK_TAG_STYLE = new KeywordStyle(TASK_TAG, true, false, true);
	public static final KeywordStyle KEYWORD_STYLE = new KeywordStyle(KEYWORD, true, false);
	public static final KeywordStyle IDENTIFIER_STYLE = new KeywordStyle(IDENTIFIER);
	public static final KeywordStyle CONSTANT_STYLE = new KeywordStyle(CONSTANT, false, true);
	public static final KeywordStyle TYPE_STYLE = new KeywordStyle(TYPE);
	public static final KeywordStyle CLASS_STYLE = new KeywordStyle(CLASS, false, true);
	public static final KeywordStyle CHAR_CONST_STYLE = new KeywordStyle(CHAR_CONST);
	public static final KeywordStyle NUMBER_STYLE = new KeywordStyle(NUMBER);
	public static final KeywordStyle DEFAULT_STYLE = new KeywordStyle(DEFAULT);
	public static final KeywordStyle OPERATOR_STYLE = new KeywordStyle(OPERATOR);
	public static final KeywordStyle BRACKET_STYLE = new KeywordStyle(BRACKET);
	public static final KeywordStyle MAIN_METHOD_STYLE = new KeywordStyle(MAIN_METHOD, true, false);
	
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
