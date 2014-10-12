package net.feichti.microjavaeditor.util;

import net.feichti.microjavaeditor.antlr4.MicroJavaParser.TypeContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Wraps a {@link VarDeclContext} that contains more than one variable.
 * 
 * @author Peter
 */
public class VarDeclWrapper
{
	private final int mIndex;
	private final VarDeclContext mContext;
	
	/**
	 * Construct a {@link VarDeclWrapper} for the specified context and variable index.
	 * 
	 * @param context The {@link VarDeclContext}
	 * @param index The index of the variable to wrap
	 */
	public VarDeclWrapper(VarDeclContext context, int index) {
		mContext = context;
		mIndex = index;
	}
	
	/**
	 * Get the identifier for this variable declaration.
	 */
	public TerminalNode getIdent() {
		return mContext.Ident(mIndex);
	}
	
	/**
	 * Get the {@link TypeContext} for this variable declaration.
	 */
	public TypeContext getType() {
		return mContext.type();
	}
	
	/**
	 * Get the wrapped {@link VarDeclContext}.
	 */
	public VarDeclContext getContext() {
		return mContext;
	}
	
	/**
	 * Get the index of the wrapped variable declaration.
	 */
	public int getIndex() {
		return mIndex;
	}
}
