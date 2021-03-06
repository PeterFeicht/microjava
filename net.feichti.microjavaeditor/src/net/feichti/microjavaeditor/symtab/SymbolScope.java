package net.feichti.microjavaeditor.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for symbols that also define their own scope.
 * 
 * @author Peter
 */
public abstract class SymbolScope extends Symbol implements Scope
{
	private Map<String, Symbol> mSymbols = new LinkedHashMap<>();
	
	/**
	 * Create a new symbol scope with the specified name and parent scope.
	 * 
	 * @param name The name of the symbol
	 * @param parent The enclosing scope
	 */
	public SymbolScope(String name, Scope parent) {
		super(name, null, parent);
	}
	
	@Override
	public Scope getParent() {
		return getScope();
	}
	
	@Override
	public void define(Symbol sym) {
		mSymbols.put(sym.getName(), sym);
		sym.setScope(this);
	}
	
	@Override
	public Symbol resolve(String name) {
		Symbol s = mSymbols.get(name);
		if(s == null && getScope() != null) {
			s = getScope().resolve(name);
		}
		return s;
	}
	
	@Override
	public boolean isDefined(String name) {
		if(mSymbols.containsKey(name)) {
			return true;
		} else if(getScope() != null) {
			return getScope().isDefined(name);
		}
		return false;
	}
	
	@Override
	public Type resolveType(String name) {
		Symbol sym = resolve(name);
		if(sym instanceof Type) {
			return (Type)sym;
		}
		return null;
	}
	
	/**
	 * Get the kind of this symbol. Subclasses can specify this way what kind of symbol they are, for example
	 * a class or method symbol.
	 */
	public abstract String getKind();
	
	@Override
	public String toString() {
		String kind = getKind();
		return (kind == null || kind.isEmpty() ? "" : kind + " ") + super.toString() + ":" + mSymbols.values();
	}
}
