package net.feichti.microjavaeditor.symtab;

/**
 * Represents a class definition symbol, as well as the associated scope.
 * 
 * @author Peter
 */
public class ClassSymbol extends SymbolScope implements Type
{
	/**
	 * Create a new class scope with the specified name and parent scope.
	 * 
	 * @param name The name of the class
	 * @param parent The enclosing scope
	 */
	public ClassSymbol(String name, Scope parent) {
		super(name, parent);
	}
	
	@Override
	public String getKind() {
		return "class";
	}
}
