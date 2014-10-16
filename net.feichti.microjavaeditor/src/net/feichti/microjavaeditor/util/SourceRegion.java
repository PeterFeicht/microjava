package net.feichti.microjavaeditor.util;

import org.eclipse.jface.text.Region;

/**
 * Extends the {@link Region} class with methods for testing overlaps and such.
 * 
 * @author Peter
 */
public class SourceRegion extends Region
{
	/**
	 * Create a new region.
	 *
	 * @param offset the offset of the region
	 * @param length the length of the region
	 */
	public SourceRegion(int offset, int length) {
		super(offset, length);
	}
	
	/**
	 * Determine whether this region contains the specified one.
	 * 
	 * @param other The Region to test
	 * @return {@code true} if {@code other} is completely contained within this region (boundaries may be
	 *         equal), {@code false} otherwise
	 */
	public boolean contains(Region other) {
		return getOffset() <= other.getOffset() &&
				getOffset() + getLength() >= other.getOffset() + other.getLength();
	}
	
	/**
	 * Determine whether this region and the specified one overlap.
	 * 
	 * @param other The Region to test
	 * @return {@code true} if the regions have at least one character in common (adjacent regions don't
	 *         overlap), {@code false} otherwise
	 */
	public boolean overlaps(Region other) {
		return getOffset() + getLength() > other.getOffset() ||
				other.getOffset() + other.getLength() > getOffset();
	}
	
	/**
	 * Determine whether this region and the specified one are adjacent.
	 * <p>
	 * For example, regions <i>(offset=1, length=1)</i> and <i>(offset=2, length=3)</i> are adjacent, regions
	 * <i>(offset=5, length=2)</i> and <i>(offset=8, length=4)</i> are not.
	 * 
	 * @param other The Region to test
	 * @return {@code true} if the regions are adjacent (but don't have characters in common), {@code false}
	 *         otherwise
	 */
	public boolean isAdjacent(Region other) {
		return getOffset() + getLength() == other.getOffset() ||
				other.getOffset() + other.getLength() == getOffset();
	}
}
