package org.tangence.java;

/**
 */
public class TangenceMessageGetPropElem extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageGetPropElem.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageGetPropElem(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
}

