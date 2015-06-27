package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class OutgoingMessage {
	private ByteBuffer buffer;
	private TangenceMessage request;
	private Future<OutgoingMessage> started = new Future<>();
	private Future<OutgoingMessage> completed = new Future<>();
	private Future<TangenceMessage> response = new Future<>();
	private boolean isReply;

	/**
	 * Constructor for OutgoingMessage.
	 * @param b ByteBuffer
	 */
	public OutgoingMessage(final ByteBuffer b) {
		this.buffer = b;
		this.isReply = false;
	}

	public OutgoingMessage(final TangenceMessage m) {
		this.request = m;
		this.buffer = m.asBuffer();
		this.isReply = m.isReply();
	}

	/**
	 * Method onStart.
	 * @return OutgoingMessage
	 * @throws TangenceException
	 */
	public OutgoingMessage onStart() throws TangenceException { this.started.done(this); return this; }
	/**
	 * Method onComplete.
	 * @return OutgoingMessage
	 * @throws TangenceException
	 */
	public OutgoingMessage onComplete() throws TangenceException { this.completed.done(this); return this; }

	/**
	 * Method started.
	 * @return boolean
	 */
	public boolean started() { return this.started.isReady(); }
	/**
	 * Method buffer.
	 * @return ByteBuffer
	 */
	public ByteBuffer buffer() { return buffer; }

	/**
	 * Method completion.
	 * @return Future
	 */
	public Future completion() { return completed; }

	/**
	 * Method request.
	 * @return TangenceMessage
	 */
	public TangenceMessage request() { return request; }
	/**
	 * Method request.
	 * @param m TangenceMessage
	 * @return OutgoingMessage
	 */
	public OutgoingMessage request(final TangenceMessage m) { this.request = m; return this; }
	/**
	 * Method response.
	 * @return Future
	 */
	public Future response() { return response; }

	public boolean isReply() { return isReply; }
}

