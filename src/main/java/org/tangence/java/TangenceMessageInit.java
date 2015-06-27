package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class TangenceMessageInit extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageInit.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageInit(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	/**
	 * Method parse.
	 * @param b ByteBuffer
	 * @return TangenceMessage
	 * @throws TangenceException
	 */
	public TangenceMessage parse(final ByteBuffer b) throws TangenceException {
		int major = (int)Types.intFromBytes(b);
		int maxMinor = (int)Types.intFromBytes(b);
		int minMinor = (int)Types.intFromBytes(b);
		System.out.printf("Init: request %d, max %d min %d%n", major, maxMinor, minMinor);
		return this;
	}

	/*
	public void onResponse(final TangenceMessage response) {
	}
	*/
}

