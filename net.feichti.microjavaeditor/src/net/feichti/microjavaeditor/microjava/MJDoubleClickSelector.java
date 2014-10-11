package net.feichti.microjavaeditor.microjava;

import net.feichti.microjavaeditor.util.MJWordDetector;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of MicroJava identifier syntax rules.
 */
public class MJDoubleClickSelector implements ITextDoubleClickStrategy
{
	protected static char[] sBrackets = { '{', '}', '(', ')', '[', ']', '"', '"' };
	
	private MJWordDetector mWordDetector = new MJWordDetector();
	protected ITextViewer mText;
	protected int mPos;
	protected int mStartPos;
	protected int mEndPos;
	
	@Override
	public void doubleClicked(ITextViewer text) {
		mPos = text.getSelectedRange().x;
		if(mPos < 0) {
			return;
		}
		
		mText = text;
		if(!selectBracketBlock()) {
			selectWord();
		}
	}
	
	/**
	 * Match the brackets at the current selection. Return <code>true</code> if successful, <code>false</code>
	 * otherwise.
	 *
	 * @return <code>true</code> if brackets match, <code>false</code> otherwise
	 */
	protected boolean matchBracketsAt() {
		char prevChar, nextChar;
		
		int i;
		int bracketIndex1 = sBrackets.length;
		int bracketIndex2 = sBrackets.length;
		
		mStartPos = -1;
		mEndPos = -1;
		
		// get the chars preceding and following the start position
		try {
			IDocument doc = mText.getDocument();
			
			prevChar = doc.getChar(mPos - 1);
			nextChar = doc.getChar(mPos);
			
			// is the char either an open or close bracket?
			for(i = 0; i < sBrackets.length; i = i + 2) {
				if(prevChar == sBrackets[i]) {
					mStartPos = mPos - 1;
					bracketIndex1 = i;
				}
			}
			for(i = 1; i < sBrackets.length; i = i + 2) {
				if(nextChar == sBrackets[i]) {
					mEndPos = mPos;
					bracketIndex2 = i;
				}
			}
			
			if(mStartPos > -1 && bracketIndex1 < bracketIndex2) {
				mEndPos = searchForClosingBracket(mStartPos, prevChar, sBrackets[bracketIndex1 + 1], doc);
				if(mEndPos > -1) {
					return true;
				}
				mStartPos = -1;
			} else if(mEndPos > -1) {
				mStartPos = searchForOpenBracket(mEndPos, sBrackets[bracketIndex2 - 1], nextChar, doc);
				if(mStartPos > -1) {
					return true;
				}
				mEndPos = -1;
			}
		} catch(BadLocationException x) {
			
		}
		
		return false;
	}
	
	/**
	 * Select the word at the current selection location. Return <code>true</code> if successful,
	 * <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if a word can be found at the current selection location, <code>false</code>
	 *         otherwise
	 */
	protected boolean matchWord() {
		IDocument doc = mText.getDocument();
		
		try {
			int pos = mPos;
			char c;
			
			while(pos >= 0) {
				c = doc.getChar(pos);
				if(!mWordDetector.isWordPart(c)) {
					break;
				}
				--pos;
			}
			
			mStartPos = pos;
			
			pos = mPos;
			int length = doc.getLength();
			
			while(pos < length) {
				c = doc.getChar(pos);
				if(!mWordDetector.isWordPart(c)) {
					break;
				}
				++pos;
			}
			
			mEndPos = pos;
			
			return true;
		} catch(BadLocationException x) {
			
		}
		
		return false;
	}
	
	/**
	 * Returns the position of the closing bracket after <code>startPosition</code>.
	 *
	 * @param startPosition - the beginning position
	 * @param openBracket - the character that represents the open bracket
	 * @param closeBracket - the character that represents the close bracket
	 * @param document - the document being searched
	 * @return the location of the closing bracket.
	 * @throws BadLocationException in case <code>startPosition</code> is invalid in the document
	 */
	protected static int searchForClosingBracket(int startPosition, char openBracket, char closeBracket,
			IDocument document) throws BadLocationException {
		int stack = 1;
		int closePosition = startPosition + 1;
		int length = document.getLength();
		char nextChar;
		
		while(closePosition < length && stack > 0) {
			nextChar = document.getChar(closePosition);
			if(nextChar == openBracket && nextChar != closeBracket) {
				stack++;
			} else if(nextChar == closeBracket) {
				stack--;
			}
			closePosition++;
		}
		
		if(stack == 0) {
			return closePosition - 1;
		}
		return -1;
		
	}
	
	/**
	 * Returns the position of the open bracket before <code>startPosition</code>.
	 *
	 * @param startPosition - the beginning position
	 * @param openBracket - the character that represents the open bracket
	 * @param closeBracket - the character that represents the close bracket
	 * @param document - the document being searched
	 * @return the location of the starting bracket.
	 * @throws BadLocationException in case <code>startPosition</code> is invalid in the document
	 */
	protected static int searchForOpenBracket(int startPosition, char openBracket, char closeBracket, IDocument document)
			throws BadLocationException {
		int stack = 1;
		int openPos = startPosition - 1;
		char nextChar;
		
		while(openPos >= 0 && stack > 0) {
			nextChar = document.getChar(openPos);
			if(nextChar == closeBracket && nextChar != openBracket) {
				stack++;
			} else if(nextChar == openBracket) {
				stack--;
			}
			openPos--;
		}
		
		if(stack == 0) {
			return openPos + 1;
		}
		return -1;
	}
	
	/**
	 * Select the area between the selected bracket and the closing bracket.
	 *
	 * @return <code>true</code> if selection was successful, <code>false</code> otherwise
	 */
	protected boolean selectBracketBlock() {
		if(matchBracketsAt()) {
			if(mStartPos == mEndPos) {
				mText.setSelectedRange(mStartPos, 0);
			} else {
				mText.setSelectedRange(mStartPos + 1, mEndPos - mStartPos - 1);
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * Select the word at the current selection.
	 */
	protected void selectWord() {
		if(matchWord()) {
			if(mStartPos == mEndPos) {
				mText.setSelectedRange(mStartPos, 0);
			} else {
				mText.setSelectedRange(mStartPos + 1, mEndPos - mStartPos - 1);
			}
		}
	}
}
