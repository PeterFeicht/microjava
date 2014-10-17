package net.feichti.microjavaeditor.microjava;

import java.util.ArrayList;
import java.util.List;

import net.feichti.microjavaeditor.util.MJColorManager;
import net.feichti.microjavaeditor.util.MJWordDetector;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WordRule;

public class MJCommentScanner extends RuleBasedScanner
{
	public static final String[] TASK_TAGS = new String[] { "TODO", "XXX", "FIXME" };
	
	public MJCommentScanner(MJColorManager colorManager) {
		IToken comment = MJColorManager.COMMENT_STYLE.getStyleToken(colorManager);
		IToken task = MJColorManager.TASK_TAG_STYLE.getStyleToken(colorManager);
		
		List<IRule> rules = new ArrayList<>();
		
		WordRule wordRule = new WordRule(new MJWordDetector(), comment);
		for(String s : TASK_TAGS) {
			wordRule.addWord(s, task);
		}
		rules.add(wordRule);
		
		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
		setDefaultReturnToken(comment);
	}
}
