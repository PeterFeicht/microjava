package net.feichti.microjavaeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * This scanner recognizes multi-line comments.
 * 
 * @author Peter
 */
public class MJPartitionScanner extends RuleBasedPartitionScanner
{
	public final static String MICROJAVA_COMMENT = "__microjava_multiline_comment";
	public final static String[] MICROJAVA_PARTITION_TYPES = new String[] { MICROJAVA_COMMENT };
	
	static class EmptyCommentDetector implements IWordDetector
	{
		@Override
		public boolean isWordStart(char c) {
			return (c == '/');
		}
		
		@Override
		public boolean isWordPart(char c) {
			return (c == '*' || c == '/');
		}
	}
	
	static class WordPredicateRule extends WordRule implements IPredicateRule
	{
		private IToken mSuccessToken;
		
		public WordPredicateRule(IToken successToken) {
			super(new EmptyCommentDetector());
			mSuccessToken = successToken;
			addWord("/**/", mSuccessToken);
		}
		
		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return super.evaluate(scanner);
		}
		
		@Override
		public IToken getSuccessToken() {
			return mSuccessToken;
		}
	}
	
	static class NestedCommentRule implements IPredicateRule
	{
		final IToken mToken;
		
		public NestedCommentRule(IToken token) {
			mToken = Objects.requireNonNull(token);
		}
		
		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int c = scanner.read();
			if(c == '/') {
				c = scanner.read();
				if(c == '*') {
					consumeToEnd(scanner);
					return mToken;
				}
				scanner.unread();
			}
			scanner.unread();
			return Token.UNDEFINED;
		}
		
		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			if(resume) {
				consumeToEnd(scanner);
				return mToken;
			}
			return evaluate(scanner);
		}
		
		public static void consumeToEnd(ICharacterScanner scanner) {
			int level = 1;
			int c = scanner.read();
			
			while(c != ICharacterScanner.EOF && level > 0) {
				final int old = c;
				c = scanner.read();
				
				if(old == '*' && c == '/') {
					level--;
					c = scanner.read();
				} else if(old == '/' && c == '*') {
					level++;
					c = scanner.read();
				}
			}
			
			// Unclosed comments consume the whole file so we don't unread in that case
			if(level == 0) {
				scanner.unread();
			}
		}
		
		@Override
		public IToken getSuccessToken() {
			return mToken;
		}
	}
	
	/**
	 * Creates the partitioner and sets up the appropriate rules.
	 */
	public MJPartitionScanner() {
		IToken comment = new Token(MICROJAVA_COMMENT);
		
		List<IPredicateRule> rules = new ArrayList<>();
		
		// Add rule for character constants
		rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\'));
		
		// Add special case word rule
		rules.add(new WordPredicateRule(comment));
		
		// Add rule for nested multi-line comments
		rules.add(new NestedCommentRule(comment));
		
		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
