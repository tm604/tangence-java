package org.tangence.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;

/**
 * Base class for server and client implementations.
 * Holds the {@link Registry} for storing classes and
 * objects, and provides the helper methods for
 * constructing and disassembling message elements.
 */
public class TangenceBase {
	private static Logger log = LoggerFactory.getLogger(TangenceBase.class.getName());

	private Registry registry = new Registry();
	private MessageBufferFactory mbf;

	public TangenceBase() {
		this.mbf = new MessageBufferFactory(registry);
	}

	public Registry registry() { return registry; }

	public TangenceMessage messageFromBuffer(final ByteBuffer b) throws TangenceException {
		final int type = b.get() & 0xFF;
		final long len = (long)b.getInt();
		final TangenceMessage m = Constants.classFromType(type, len, registry);
		m.factory(mbf);
		return m;
	}
}
