package org.tangence.java;

/**
 */
public class TangenceMessageOK extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageOK.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageOK(int type, long length, final Registry registry) {
		super(type, length, registry);
	}
	public boolean isReply() { return true; }
}

