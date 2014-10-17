package net.feichti.microjavaeditor.symtab;

/**
 * Represents a variable definition.
 * 
 * @author Peter
 */
public class VariableSymbol extends Symbol
{
	/**
	 * Create a new variable symbol with the specified name and type.
	 * 
	 * @param name The name of the symbol
	 * @param type The type of the symbol
	 */
	public VariableSymbol(String name, Type type) {
		super(name, type);
	}
}
