package net.feichti.microjavaeditor.microjava;

import java.util.ArrayList;
import java.util.List;

import net.feichti.microjavaeditor.util.MJColorManager;
import net.feichti.microjavaeditor.util.MJWhitespaceDetector;
import net.feichti.microjavaeditor.util.MJWordDetector;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

public class MJCodeScanner extends RuleBasedScanner
{
	/**
	 * Rule to detect MicroJava operators.
	 */
	private static final class OperatorRule implements IRule
	{
		private final IToken mToken;
		
		/**
		 * Create a new operator rule.
		 *
		 * @param token Token to use for this rule
		 */
		public OperatorRule(IToken token) {
			mToken = token;
		}
		
		/**
		 * Is this character an operator character?
		 *
		 * @param character Character to determine whether it is an operator character
		 * @return {@code true} iff the character is an operator, {@code false} otherwise
		 */
		public static boolean isOperator(char character) {
			for(int index = 0; index < OPERATORS.length; index++) {
				if(OPERATORS[index] == character) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int character = scanner.read();
			
			if(isOperator((char)character)) {
				do {
					character = scanner.read();
				} while(isOperator((char)character));
				scanner.unread();
				return mToken;
			}
			
			scanner.unread();
			return Token.UNDEFINED;
		}
	}
	
	/**
	 * Rule to detect MicroJava brackets.
	 */
	private static final class BracketRule implements IRule
	{
		private final IToken mToken;
		
		/**
		 * Create a new bracket rule.
		 *
		 * @param token Token to use for this rule
		 */
		public BracketRule(IToken token) {
			mToken = token;
		}
		
		/**
		 * Is this character a bracket character?
		 *
		 * @param character Character to determine whether it is a bracket character
		 * @return {@code true} iff the character is a bracket, {@code false} otherwise
		 */
		public static boolean isBracket(char character) {
			for(int index = 0; index < BRACKETS.length; index++) {
				if(BRACKETS[index] == character) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int character = scanner.read();
			
			if(isBracket((char)character)) {
				do {
					character = scanner.read();
				} while(isBracket((char)character));
				scanner.unread();
				return mToken;
			}
			
			scanner.unread();
			return Token.UNDEFINED;
		}
	}
	
	public static final String[] KEYWORDS = { "program", "class", "if", "else", "while", "switch", "case", "default",
			"break", "return", "final", "new", "read", "print" };
	public static final String[] CONSTANTS = { "null" };
	public static final String[] TYPES = { "int", "char", "void" };
	public static final char[] OPERATORS = { ';', '.', '=', '/', '+', '-', '*', '<', '>', ':', '?', '!', ',', '%', '&',
			'|' };
	public static final char[] BRACKETS = { '(', ')', '{', '}', '[', ']' };
	
	public MJCodeScanner(MJColorManager colorManager) {
		IToken keyword = MJColorManager.KEYWORD_STYLE.getStyleToken(colorManager);
		IToken operator = MJColorManager.OPERATOR_STYLE.getStyleToken(colorManager);
		IToken bracket = MJColorManager.BRACKET_STYLE.getStyleToken(colorManager);
		IToken constant = MJColorManager.CONSTANT_STYLE.getStyleToken(colorManager);
		IToken type = MJColorManager.TYPE_STYLE.getStyleToken(colorManager);
		IToken number = MJColorManager.NUMBER_STYLE.getStyleToken(colorManager);
		IToken charconst = MJColorManager.CHAR_CONST_STYLE.getStyleToken(colorManager);
		IToken other = MJColorManager.DEFAULT_STYLE.getStyleToken(colorManager);
		IToken main = MJColorManager.MAIN_METHOD_STYLE.getStyleToken(colorManager);
		
		List<IRule> rules = new ArrayList<>();
		
		rules.add(new SingleLineRule("'", "'", charconst, '\\'));
		rules.add(new NumberRule(number));
		rules.add(new OperatorRule(operator));
		rules.add(new BracketRule(bracket));
		rules.add(new WhitespaceRule(new MJWhitespaceDetector()));
		
		WordRule wordRule = new WordRule(new MJWordDetector(), other);
		for(String s : KEYWORDS) {
			wordRule.addWord(s, keyword);
		}
		for(String s : TYPES) {
			wordRule.addWord(s, type);
		}
		for(String s : CONSTANTS) {
			wordRule.addWord(s, constant);
		}
		wordRule.addWord("main", main);
		rules.add(wordRule);
		
		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
