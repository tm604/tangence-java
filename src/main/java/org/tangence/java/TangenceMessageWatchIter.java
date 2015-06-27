package org.tangence.java;

/**
 */
public class TangenceMessageWatchIter extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageWatchIter.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageWatchIter(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
}

