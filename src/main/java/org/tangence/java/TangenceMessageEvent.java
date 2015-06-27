package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class TangenceMessageEvent extends TangenceMessage {
	private long id;
	private String name;
	/**
	 * Constructor for TangenceMessageEvent.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageEvent(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessageEvent parse(final ByteBuffer b) {
		this.id = Types.intFromBytes(b);
		this.name = Types.strFromBytes(b);
		log.debug(String.format("Event %s on object %d", name, id));
		final TangenceObjectProxy obj = registry().getObject(id);
		obj.onEvent(name);
		return this;
	}
	public long id() { return this.id; }
	public String name() { return this.name; }
}

