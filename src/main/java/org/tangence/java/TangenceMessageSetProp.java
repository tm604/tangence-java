package org.tangence.java;

/**
 */
public class TangenceMessageSetProp extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageSetProp.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageSetProp(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
}

