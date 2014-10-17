package net.feichti.microjavaeditor.symtab;

/**
 * Represents a constant definition.
 * 
 * @author Peter
 */
public class ConstantSymbol extends Symbol
{
	private Object mValue;
	
	/**
	 * Create a new constant symbol with the specified name, type and value.
	 * 
	 * @param name The name of the symbol
	 * @param type The type of the symbol
	 * @param value The value of the constant
	 */
	public ConstantSymbol(String name, Type type, Object value) {
		super(name, type);
		mValue = value;
	}
	
	/**
	 * Get the value of this constant definition.
	 */
	public Object getValue() {
		return mValue;
	}
}
