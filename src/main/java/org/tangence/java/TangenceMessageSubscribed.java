package org.tangence.java;

/**
 */
public class TangenceMessageSubscribed extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageSubscribed.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageSubscribed(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
	public boolean isReply() { return true; }
}

