package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class TangenceMessageGetRoot extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageGetRoot.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageGetRoot(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	/**
	 * Method parse.
	 * @param b ByteBuffer
	 * @return TangenceMessage
	 * @throws TangenceException
	 */
	public TangenceMessage parse(final ByteBuffer b) throws TangenceException {
		final String identity = Types.strFromBytes(b);
		System.out.printf("Get root (identity %s)%n", identity);
		return this;
	}
}

