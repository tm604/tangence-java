package org.tangence.java;

/**
 */
public class TangenceMessageUnsubscribe extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageUnsubscribe.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageUnsubscribe(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
}

