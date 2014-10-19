package net.feichti.microjavaeditor.symtab;

import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
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
	private Map<Symbol, ParseTree> mDeclarations;
	
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
	
	/**
	 * Get the declaration map, a map associating symbols with their declaration sites.
	 */
	public Map<Symbol, ParseTree> getDeclarations() {
		return mDeclarations;
	}

	/**
	 * Set the declaration map.
	 */
	public void setDeclarations(Map<Symbol, ParseTree> declarations) {
		mDeclarations = declarations;
	}

	/**
	 * Resolve a symbol in the specified context. The symbol is resolved in the scope associated with the
	 * specified context, or the next parent context, if one can be found.
	 * 
	 * @param name The symbol name to resolve
	 * @param context The context to resolve the symbol in
	 * @return The symbol, or {@code null} if no scope for the context is found or no symbol with the
	 *         specified name is defined
	 */
	public Symbol resolve(String name, ParseTree context) {
		ParseTree next = context;
		Scope scope = null;
		
		while(scope == null && next != null) {
			scope = mScopes.get(next);
			next = next.getParent();
		}
		
		if(scope != null) {
			return scope.resolve(name);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return mUniverse.toString();
	}
}
