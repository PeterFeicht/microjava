package net.feichti.microjavaeditor.util;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class MJWhitespaceDetector implements IWhitespaceDetector
{
	@Override
	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
