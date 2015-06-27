package org.tangence.java;

/**
 * @author tom
 * @version $Revision: 1.0 $
 */
public class TangenceProperty {
	private int						dimension;
	private String					type;
	private boolean					smashed = false;
	private	String					name;

	public TangenceProperty() { this.name = "unknown"; }

	/**
	 * Method name.
	 * @return String
	 */
	public String name() { return name; }
	/**
	 * Method name.
	 * @param name String
	 * @return TangenceProperty
	 */
	public TangenceProperty name(final String name) { this.name = name; return this; }

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return "TangenceProperty[" + name + "]";
	}

	/**
	 * Method dimension.
	
	 * @return int */
	public int dimension() { return dimension; }

	/**
	 * Method dimension.
	 * @param dimension int
	
	 * @return TangenceProperty */
	public TangenceProperty dimension(final int dimension) { this.dimension = dimension; return this; }

	/**
	 * Method type.
	
	 * @return String */
	public String type() { return type; }

	/**
	 * Method type.
	 * @param type String
	
	 * @return TangenceProperty */
	public TangenceProperty type(final String type) { this.type = type; return this; }

	/**
	 * Method smashed.
	
	 * @return boolean */
	public boolean smashed() { return smashed; }

	/**
	 * Method smashed.
	 * @param smashed boolean
	
	 * @return TangenceProperty */
	public TangenceProperty smashed(final boolean smashed) { this.smashed = smashed; return this; }
}

