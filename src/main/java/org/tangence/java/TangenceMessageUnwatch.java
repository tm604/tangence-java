package org.tangence.java;

/**
 */
public class TangenceMessageUnwatch extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageUnwatch.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageUnwatch(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
}

