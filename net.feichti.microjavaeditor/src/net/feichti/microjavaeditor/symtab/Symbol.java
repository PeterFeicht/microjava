package net.feichti.microjavaeditor.symtab;

/**
 * Represents a symbol in the symbol table.
 * 
 * @author Peter
 */
public class Symbol
{
	private final String mName;
	private Type mType;
	private Scope mScope;
	
	/**
	 * Create a new symbol with the specified name and type without a scope.
	 * 
	 * @param name The symbol name
	 * @param type The type of the symbol
	 */
	public Symbol(String name, Type type) {
		this(name, type, null);
	}
	
	/**
	 * Create a new symbol with the specified name, scope and type.
	 * 
	 * @param name The symbol name
	 * @param type The type of the symbol
	 * @param scope The scope the symbol is defined in
	 */
	public Symbol(String name, Type type, Scope scope) {
		mName = name;
		mScope = scope;
		mType = type;
	}
	
	/**
	 * Get the name of this symbol.
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Get the type of this symbol.
	 */
	public Type getType() {
		return mType;
	}
	
	/**
	 * Set the type of this symbol.
	 */
	public void setType(Type type) {
		mType = type;
	}
	
	/**
	 * Get the scope this symbol is defined in.
	 */
	public Scope getScope() {
		return mScope;
	}
	
	/**
	 * Set the scope this symbol is defined in.
	 */
	public void setScope(Scope scope) {
		mScope = scope;
	}
	
	@Override
	public String toString() {
		if(mType != null) {
			return '<' + getName() + ":" + mType + '>';
		}
		return getName();
	}
}
