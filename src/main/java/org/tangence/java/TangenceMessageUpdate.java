package org.tangence.java;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

/**
 */
public class TangenceMessageUpdate extends TangenceMessage {
	private long id;
	private String name;
	private int changeType;

	/**
	 * Constructor for TangenceMessageUpdate.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageUpdate(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessageUpdate parse(final ByteBuffer b) {
		this.id = Types.intFromBytes(b);
		this.name = Types.strFromBytes(b);
		this.changeType = (int) Types.intFromBytes(b);
		final TangenceObjectProxy obj = object();
		log.debug(String.format("Update object %d (%s) property %s", id, obj.definition().name(), name));
		final List<Object> param = new ArrayList<Object>();
		while(b.hasRemaining()) {
			param.add(factory().anyFromBytes(b));
		}
		obj.applyUpdate(this, param);
		return this;
	}

	public TangenceObjectProxy object() {
		final TangenceObjectProxy obj = registry().getObject(id);
		return obj;
	}

	public long id() { return this.id; }
	public String name() { return this.name; }
	public int changeType() { return this.changeType; }
}

