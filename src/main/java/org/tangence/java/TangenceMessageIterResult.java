package org.tangence.java;

/**
 */
public class TangenceMessageIterResult extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageIterResult.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageIterResult(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
}

