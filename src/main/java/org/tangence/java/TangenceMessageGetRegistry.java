package org.tangence.java;

import java.nio.ByteBuffer;

/**
 */
public class TangenceMessageGetRegistry extends TangenceMessage {
	/**
	 * Constructor for TangenceMessageGetRegistry.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageGetRegistry(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	/**
	 * Method parse.
	 * @param b ByteBuffer
	 * @return TangenceMessage
	 * @throws TangenceException
	 */
	public TangenceMessage parse(final ByteBuffer b) throws TangenceException {
		log.debug("Get registry");
		return this;
	}

	/**
	 * Method onResponse.
	 * @param response TangenceMessage
	 */
	public void onResponse(final TangenceMessage response) {
		log.debug("We now have registry info... {}", response);
	}
}

