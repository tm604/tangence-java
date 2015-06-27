package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class TangenceMessageInited extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageInited.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageInited(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	/**
	 * Method parse.
	 * @param b ByteBuffer
	 * @return TangenceMessage
	 */
	public TangenceMessage parse(final ByteBuffer b) {
		int major = (int)Types.intFromBytes(b);
		int minor = (int)Types.intFromBytes(b);
		log.debug("INITED: {}, {}", major, minor);
		return this;
	}
	public boolean isReply() { return true; }
}

