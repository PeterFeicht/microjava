package net.feichti.microjavaeditor;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MJEditorMessages
{
	private static final String RESOURCE_BUNDLE = "net.feichti.microjavaeditor.MJEditorMessages";
	
	private static ResourceBundle sResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
	
	public static String getString(String key) {
		try {
			return sResourceBundle.getString(key);
		} catch(MissingResourceException e) {
			return "!" + key + "!";
		}
	}
	
	public static String format(String key, Object... args) {
		try {
			String val = sResourceBundle.getString(key);
			return MessageFormat.format(val, args);
		} catch(MissingResourceException ex) {
			return "!" + key + "!";
		}
	}
	
	public static ResourceBundle getResourceBundle() {
		return sResourceBundle;
	}
}
