package net.feichti.microjavaeditor.symtab;

/**
 * A local scope inside a block statement.
 * 
 * @author Peter
 */
public class LocalScope extends AbstractScope
{
	/**
	 * Create a new local scope with the specified parent.
	 * 
	 * @param parent The enclosing scope
	 */
	public LocalScope(Scope parent) {
		super(parent);
	}
	
	@Override
	public String getName() {
		return "local";
	}
	
}
