package org.tangence.java;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class TangenceMessageResult extends TangenceMessage {
	private static Logger log = LoggerFactory.getLogger(TangenceMessageResult.class.getName());

	private Object result;

	/**
	 * Constructor for TangenceMessageResult.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageResult(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	/**
	 * Method parse.
	 * @param b ByteBuffer
	 * @return TangenceMessage
	 */
	public TangenceMessage parse(final ByteBuffer b) {
		final Object o = factory().anyFromBytes(b);
		log.debug(String.format("Result: had %s", o));
		result = o;
		return this;
	}

	public Object result() { return result; }

	public boolean isReply() { return true; }
}

