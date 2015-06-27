package org.tangence.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 */
public class TangenceMessageSubscribe extends TangenceMessage {
	private TangenceObjectProxy obj;
	private TangenceEvent event;

	/**
	 * Constructor for TangenceMessageSubscribe.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageSubscribe(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessageSubscribe obj(final TangenceObjectProxy obj) {
		this.obj = obj;
		return this;
	}
	
	public TangenceMessageSubscribe event(final TangenceEvent event) {
		this.event = event;
		return this;
	}

	public byte[] payload() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(Types.bytesForInt(obj.id()));
			os.write(Types.bytesForStr(event.name()));
		} catch(final IOException e) {
			log.error("IO Exception: " + e.getMessage());
		}
		return os.toByteArray();
	}
}

