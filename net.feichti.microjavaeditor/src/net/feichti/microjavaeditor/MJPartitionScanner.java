package net.feichti.microjavaeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
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
	public final static String MICROJAVA_COMMENT = "__microjava_multiline_comment"; //$NON-NLS-1$
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
	
	// TODO fix nested comments
	static class NestedCommentRule implements IPredicateRule
	{
		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public IToken getSuccessToken() {
			// TODO Auto-generated method stub
			return null;
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
		
		// Add rules for multi-line comments
		rules.add(new MultiLineRule("/*", "*/", comment, (char)0, true));
		
		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
