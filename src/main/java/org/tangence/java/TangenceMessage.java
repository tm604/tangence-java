package org.tangence.java;

import static org.tangence.java.Constants.*;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for messages.
 * @author tom
 * @version $Revision: 1.0 $
 */
public class TangenceMessage {
	protected static Logger log = LoggerFactory.getLogger(TangenceMessage.class.getName());

	private int type;
	private long length;
	private Future<TangenceMessage> completion = new Future<>();

	private MessageBufferFactory factory;
	private Registry registry;

	/**
	 * Constructor for TangenceMessage.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessage(final int type, final long length, final Registry registry) {
		this.type = type;
		this.length = length;
		this.registry = registry;
	}
	protected TangenceMessage factory(final MessageBufferFactory factory) {
		this.factory = factory;
		return this;
	}

	protected MessageBufferFactory factory() { return this.factory; }

	protected Registry registry() { return registry; }

	/**
	 * Returns true if this a response to a message from the other side.
	 * Returns false for standalone messages.
	 * @return boolean
	 */
	public boolean isResponse() { return (type & 0x80) == 0x80; }

	/**
	 * Method parse.
	 * @param b ByteBuffer
	 * @return TangenceMessage
	 * @throws TangenceException
	 */
	public TangenceMessage parse(final ByteBuffer b) throws TangenceException {
		log.debug("Unhandled message type: {} ({})", messageName(type()), type());
		return this;
	}

	/**
	 * Method typeFromBuffer.
	 * @param b ByteBuffer
	 * @return int
	 */
	public int typeFromBuffer(final ByteBuffer b) {
		final int pos = b.position();
		int type = b.get() & 0xFF;
		b.position(pos);
		return type;
	}

	public ByteBuffer asBuffer() {
		final byte[] payload = payload();
		final ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + payload.length);
		buffer.put((byte)type); // single byte, write as-is
		buffer.putInt(payload.length); // uint32, default is always big-endian, so again write as-is
		buffer.put(payload); // 0..n byte buffer, write as-is
		buffer.flip();
		return buffer;
	}

	/**
	 * Method length.
	 * @return long
	 */
	public long length() { return length; }

	/**
	 * Method type.
	 * @return int
	 */
	public int type() { return type; }

	/**
	 * Method name.
	 * @return String
	 */
	public String name() { return messageName(type()); }

	/**
	 * Method onResponse.
	 * @param response TangenceMessage
	 */
	public void onResponse(final TangenceMessage response) throws TangenceException {
		log.debug("sent {}, had response {}", this.name(), response.name());
		completion().done(response);
	}

	public byte[] payload() {
		return new byte[0];
	}

	public Future completion() { return completion; }

	public boolean isReply() { return (type & 0x80) != 0; }
}
