package net.feichti.microjavaeditor.symtab;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

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
	private ParseTreeProperty<Scope> mScopes;
	
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
	
	/**
	 * Get the universe (a global scope) for this symbol table).
	 */
	public GlobalScope getUniverse() {
		return mUniverse;
	}
	
	/**
	 * Get the {@link ParseTreeProperty} for the scope annotations.
	 */
	public ParseTreeProperty<Scope> getScopes() {
		return mScopes;
	}

	/**
	 * Set the {@link ParseTreeProperty} for the scope annotations.
	 */
	public void setScopes(ParseTreeProperty<Scope> scopes) {
		mScopes = scopes;
	}

	@Override
	public String toString() {
		return mUniverse.toString();
	}
}
