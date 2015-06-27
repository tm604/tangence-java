package org.tangence.java;

/**
 */
public class TangenceMessageWatching extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageWatching.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageWatching(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
	public boolean isReply() { return true; }
}

