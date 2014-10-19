package net.feichti.microjavaeditor.symtab;

/**
 * Represents a built-in MicroJava function.
 * 
 * @author Peter
 */
public class BuiltinFunctionSymbol extends Symbol implements Type
{
	/**
	 * Create a new built-in function symbol for the specified name.
	 * 
	 * @param name The name of the function
	 */
	public BuiltinFunctionSymbol(String name) {
		super(name, null);
	}
}
