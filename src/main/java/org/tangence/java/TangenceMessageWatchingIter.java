package org.tangence.java;

/**
 */
public class TangenceMessageWatchingIter extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageWatchingIter.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageWatchingIter(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
}

