package net.feichti.microjavaeditor.util;

import org.eclipse.jface.text.rules.IWordDetector;

public class MJWordDetector implements IWordDetector
{
	@Override
	public boolean isWordStart(char c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_');
	}
	
	@Override
	public boolean isWordPart(char c) {
		return isWordStart(c) || c >= '0' && c <= '9';
	}
}
