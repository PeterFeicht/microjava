package net.feichti.microjavaeditor.symtab;

/**
 * Represents a built-in MicroJava type. There are only two built-in types, {@code int} and {@code char}.
 * 
 * @author Peter
 */
public class BuiltinTypeSymbol extends Symbol implements Type
{
	/**
	 * Create a new built-in type symbol for the specified name.
	 * 
	 * @param name The name of the type
	 */
	public BuiltinTypeSymbol(String name) {
		super(name, null);
	}
}
