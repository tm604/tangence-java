package org.tangence.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.tangence.java.Constants.*;

/**
 */
public class TangenceMessageGetProp extends TangenceMessage {
	private TangenceObjectProxy obj;
	private TangenceProperty prop;

	/**
	 * Constructor for TangenceMessageGetProp.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageGetProp(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessageGetProp obj(final TangenceObjectProxy obj) {
		this.obj = obj;
		return this;
	}
	
	public TangenceMessageGetProp property(final TangenceProperty prop) {
		this.prop = prop;
		return this;
	}

	public byte[] payload() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(Types.bytesForInt(obj.id()));
			os.write(Types.bytesForStr(prop.name()));
		} catch(final IOException e) {
			log.error("IO Exception: " + e.getMessage());
		}
		return os.toByteArray();
	}

	public void onResponse(final TangenceMessage response) throws TangenceException {
		final Future f = completion();
		if(response.type() == MSG_RESULT) {
			final Object rslt = ((TangenceMessageResult) response).result();
			log.debug(String.format("Property result was %s", rslt));
			try {
				f.done(rslt);
				log.debug(String.format("Marked completion %s as done", f));
			} catch(TangenceException e) {
				log.error(e.getMessage());
				f.fail(e);
			}
		} else if(response.type() == MSG_ERROR) {
			final TangenceMessageError err = (TangenceMessageError) response;
			log.warn(String.format("Had an error: %s", err.text()));
			f.fail(new TangenceException(err.text()));
		}
	}
}
