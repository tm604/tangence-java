package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class TangenceMessageDestroy extends TangenceMessage {
	private long id;
	/**
	 * Constructor for TangenceMessageDestroy.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageDestroy(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessageDestroy parse(final ByteBuffer b) {
		this.id = Types.intFromBytes(b);
		final TangenceObjectProxy obj = object();
		log.debug(String.format("Destroy object %d (%s)", id, obj.definition().name()));
		return this;
	}

	public TangenceObjectProxy object() {
		final TangenceObjectProxy obj = registry().getObject(id);
		return obj;
	}

	public long id() { return this.id; }
}

