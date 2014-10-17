package net.feichti.microjavaeditor.symtab;

/**
 * Represents a MicroJava symbol table, defines the symbols available in every MicroJava program.
 * <p>
 * Subclasses may override {@link #initUniverse()} to control which global symbols are created when a symbol
 * table is instantiated.
 * 
 * @author Peter
 */
public class SymbolTable
{
	private final GlobalScope mUniverse = new GlobalScope();
	
	/**
	 * Create a new symbol table with the default global symbols.
	 */
	public SymbolTable() {
		initUniverse();
	}
	
	protected void initUniverse() {
		mUniverse.define(new BuiltinTypeSymbol("int"));
		mUniverse.define(new BuiltinTypeSymbol("char"));
		
		// Also define void pseudo-type
		mUniverse.define(new BuiltinTypeSymbol("void"));
	}
	
	@Override
	public String toString() {
		return mUniverse.toString();
	}
}
