package net.feichti.microjavaeditor.symtab;

/**
 * Represents a method definition symbol, as well as the associated scope.
 * 
 * @author Peter
 */
public class MethodSymbol extends SymbolScope
{
	/**
	 * Create a new method symbol with the specified name, return type and parent scope.
	 * 
	 * @param name The name of the method
	 * @param returnType The return type of the method
	 * @param parent The enclosing scope
	 */
	public MethodSymbol(String name, Type returnType, Scope parent) {
		super(name, parent);
		setType(returnType);
	}
	
	@Override
	public String getKind() {
		return "method";
	}
}
