package org.tangence.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 */
public class TangenceMessageWatch extends TangenceMessage {
	private TangenceObjectProxy obj;
	private TangenceProperty property;
	private boolean wantInitial;

	/**
	 * Constructor for TangenceMessageWatch.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageWatch(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessageWatch obj(final TangenceObjectProxy obj) {
		this.obj = obj;
		return this;
	}
	
	public TangenceMessageWatch property(final TangenceProperty property) {
		this.property = property;
		return this;
	}

	public TangenceMessageWatch wantInitial(final boolean wantInitial) {
		this.wantInitial = wantInitial;
		return this;
	}

	public byte[] payload() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(Types.bytesForInt(obj.id()));
			os.write(Types.bytesForStr(property.name()));
			os.write(Types.bytesForBool(wantInitial));
		} catch(final IOException e) {
			log.error("IO Exception: " + e.getMessage());
		}
		return os.toByteArray();
	}
}

