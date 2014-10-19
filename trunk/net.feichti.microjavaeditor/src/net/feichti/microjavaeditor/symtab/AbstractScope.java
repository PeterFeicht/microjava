package net.feichti.microjavaeditor.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract base class for scope implementations.
 * 
 * @author Peter
 */
public abstract class AbstractScope implements Scope
{
	Map<String, Symbol> mSymbols = new LinkedHashMap<>();
	private final Scope mParent;
	
	protected AbstractScope(Scope enclosing) {
		mParent = enclosing;
	}
	
	@Override
	public Scope getParent() {
		return mParent;
	}
	
	@Override
	public void define(Symbol sym) {
		mSymbols.put(sym.getName(), sym);
		sym.setScope(this);
	}
	
	@Override
	public boolean isDefined(String name) {
		if(mSymbols.containsKey(name)) {
			return true;
		} else if(mParent != null) {
			return mParent.isDefined(name);
		}
		return false;
	}
	
	@Override
	public Symbol resolve(String name) {
		Symbol s = mSymbols.get(name);
		if(s == null && mParent != null) {
			s = mParent.resolve(name);
		}
		return s;
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
	 * Determine whether a symbol from this scope is hiding a symbol from an enclosing scope.
	 * 
	 * @param sym The symbol to check
	 * @return {@code true} if the symbol is defined in this scope and a symbol with the same name is defined
	 *         in an enclosing scope, {@code false} otherwise
	 */
	public boolean isHiding(Symbol sym) {
		if(mSymbols.containsKey(sym.getName()) && mParent != null) {
			return mParent.resolve(sym.getName()) != null;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return mSymbols.keySet().toString();
	}
}
