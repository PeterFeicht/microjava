package net.feichti.microjavaeditor.symtab;

/**
 * Represents the global scope.
 * 
 * @author Peter
 */
public class GlobalScope extends AbstractScope
{
	/**
	 * Create a new global scope.
	 */
	public GlobalScope() {
		super(null);
	}
	
	@Override
	public String getName() {
		return "global";
	}
}
