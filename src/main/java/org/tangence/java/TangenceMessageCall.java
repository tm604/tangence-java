package org.tangence.java;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 */
public class TangenceMessageCall extends TangenceMessage {
	private TangenceObjectProxy obj;
	private TangenceMethod method;
	private List<Object> arguments = new ArrayList<Object>();

	/**
	 * Constructor for TangenceMessageCall.
	 * @param type int
	 * @param length long
	 */
	public TangenceMessageCall(int type, long length, final Registry registry) {
		super(type, length, registry);
	}

	public TangenceMessageCall method(final TangenceMethod method) {
		this.method = method;
		return this;
	}

	public TangenceMessageCall obj(final TangenceObjectProxy obj) {
		this.obj = obj;
		return this;
	}
	
	public TangenceMethod method() {
		return method;
	}

	public TangenceMessageCall arguments(final Object ... args) {
		for(final Object o : args) {
			arguments.add(o);
		}
		return this;
	}

	public byte[] payload() {
		// (byte)MSG_CALL
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(Types.bytesForInt(obj.id()));
			os.write(Types.bytesForStr(method.name()));
			int idx = 0;
			for(final String argtype : method.arguments()) {
				if(argtype.equals("str")) {
					final byte[] bytes = Types.bytesForStr((String)arguments.get(idx));
					os.write(bytes, 0, bytes.length);
				} else if(argtype.equals("int")) {
					final byte[] bytes = Types.bytesForInt((Integer)arguments.get(idx));
					os.write(bytes, 0, bytes.length);
				} else if(argtype.equals("bool")) {
					final byte[] bytes = Types.bytesForBool((Boolean)arguments.get(idx));
					os.write(bytes, 0, bytes.length);
				} else if(argtype.equals("list(str)")) {
					final byte[] bytes = Types.bytesForList((List<String>)arguments.get(idx), String.class);
					os.write(bytes, 0, bytes.length);
				} else if(argtype.equals("list(int)")) {
					final byte[] bytes = Types.bytesForList((List<Integer>)arguments.get(idx), Integer.class);
					os.write(bytes, 0, bytes.length);
				} else {
					log.error(String.format("Unhandled type [%s]", argtype));
				}
				++idx;
			}
		} catch(final IOException e) {
			log.error("IO Exception: " + e.getMessage());
		}
		return os.toByteArray();
	}

	public void onResponse(final TangenceMessage response) {
		final Object rslt = ((TangenceMessageResult) response).result();
		log.debug(String.format("Method result was %s", rslt));
		try {
			final Future<Object> f = completion();
			f.done(rslt);
			log.debug(String.format("Marked completion %s as done", f));
		} catch(TangenceException e) {
			log.error(e.getMessage());
		}
	}
}
