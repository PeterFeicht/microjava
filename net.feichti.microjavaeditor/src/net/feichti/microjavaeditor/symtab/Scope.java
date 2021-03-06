package net.feichti.microjavaeditor.symtab;

/**
 * Defines the interface for a symbol table scope.
 * 
 * @author Peter
 */
public interface Scope
{
	/**
	 * Get the name of this scope.
	 */
	public String getName();
	
	/**
	 * Get the enclosing scope.
	 * 
	 * @return The enclosing scope, or {@code null} if this is the global scope
	 */
	public Scope getParent();
	
	/**
	 * Define a symbol in this scope.
	 * 
	 * @param sym The symbol to define
	 */
	public void define(Symbol sym);
	
	/**
	 * Determine whether a symbol is defined.
	 * 
	 * @param name The symbol name to check
	 * @return {@code true} if the symbol is defined in this or any enclosing scope, {@code false} otherwise
	 */
	public boolean isDefined(String name);
	
	/**
	 * Resolve a symbol for the specified name.
	 * 
	 * @param name The symbol name to resolve
	 * @return The symbol, or {@code null} if no symbol with the specified name is defined
	 */
	public Symbol resolve(String name);
	
	/**
	 * Resolve a type symbol for the specified name.
	 * 
	 * @param name The type name to resolve
	 * @return The type, or {@code null} if no type symbol with the specified name is defined
	 */
	public Type resolveType(String name);
}
