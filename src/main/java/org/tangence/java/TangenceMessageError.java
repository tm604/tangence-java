package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class TangenceMessageError extends TangenceMessage {
	private String text;

	/**
	 * Constructor for TangenceMessageError.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageError(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessage parse(final ByteBuffer b) {
		final String o = Types.strFromBytes(b);
		log.debug("ERROR: had {}", o);
		text = o;
		return this;
	}

	public String text() { return text; }
	public boolean isReply() { return true; }
}

