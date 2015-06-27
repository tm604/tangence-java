package org.tangence.java;

public interface Connection {
	public OutgoingMessage writeMessage(final byte type, final byte[] payload) throws TangenceException;
	public OutgoingMessage writeMessage(final TangenceMessage m) throws TangenceException;
	public Registry registry();
}
