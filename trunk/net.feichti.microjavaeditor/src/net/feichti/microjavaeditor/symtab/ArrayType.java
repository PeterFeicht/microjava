package net.feichti.microjavaeditor.symtab;

/**
 * Represents an array type with an element type.
 * 
 * @author Peter
 */
public class ArrayType implements Type
{
	private final Type mElementType;
	
	public ArrayType(Type elementType) {
		mElementType = elementType;
	}
	
	@Override
	public String getName() {
		return mElementType.getName() + "[]";
	}
	
	/**
	 * Get the type of elements in this array.
	 */
	public Type getElementType() {
		return mElementType;
	}
}
