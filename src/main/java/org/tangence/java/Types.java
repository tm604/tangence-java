package org.tangence.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.tangence.java.Constants.*;

/**
 * Type mapping
 *
 * Most of the types map cleanly onto Java:
 *
 * bool - boolean
 * integer - int or long
 * str - String
 * obj - Object
 * list - List
 * dict - Map
 * struct - Object
 * any - Object
 *
 * Classes
 *
 * Since Java does not have multiple inheritance there may
 * be issues with multiple 'isa' definitions.
 *
 * Methods are created as-is.
 *
 * Events are handled by an event bus.
 *
 * Smashed properties will automatically assign to the
 * smash event.
 *
 * There are 4 hardcoded struct IDs:
 * * 1 - Tangence.Class
 * * 2 - Tangence.Method
 * * 3 - Tangence.Event
 * * 4 - Tangence.Property
 *
 * These structs are predefined in all Tangence implementations:
 * we should never see definitions for these across the wire.
 *
 * @version $Revision: 1.0 $
 */
public class Types {
	private static Logger log = LoggerFactory.getLogger(Types.class.getName());

	/**
	 * Returns the byte data representation for the given number
	 * @param v long
	 * @return byte[]
	 */
	public static byte[] bytesForInt(long v) {
		if(v < 0) {
			if(-v < 127) {
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_SINT8),
					(byte)v
				};
			} else if(-v < 32767) {
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_SINT16),
					(byte)((v & 0xFF00) >> 8),
					(byte)(v & 0xFF)
				};
			} else if(-v < 2147483647) {
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_SINT32),
					(byte)((v & 0xFF000000) >> 24),
					(byte)((v & 0x00FF0000) >> 16),
					(byte)((v & 0x0000FF00) >>  8),
					(byte)( v & 0xFF)
				};
			} else {
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_SINT64),
					(byte)((v & 0xFF00000000000000L) >> 56),
					(byte)((v & 0x00FF000000000000L) >> 48),
					(byte)((v & 0x0000FF0000000000L) >> 40),
					(byte)((v & 0x000000FF00000000L) >> 32),
					(byte)((v & 0x00000000FF000000L) >> 24),
					(byte)((v & 0x0000000000FF0000L) >> 16),
					(byte)((v & 0x000000000000FF00L) >>  8),
					(byte)( v & 0xFF)
				};
			}
		} else {
			if(v < 127) {
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_UINT8),
					(byte)v
				};
			} else if(v < 32767) {
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_UINT16),
					(byte)((v & 0xFF00) >> 8),
					(byte)(v & 0xFF)
				};
			} else if(v < 2147483647) {
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_UINT32),
					(byte)((v & 0xFF000000) >> 24),
					(byte)((v & 0x00FF0000) >> 16),
					(byte)((v & 0x0000FF00) >>  8),
					(byte)( v & 0xFF)
				};
			} else {
				log.info("Actual value we have will be: {}", v);
				return new byte[] {
					(byte)(DATA_NUMBER | DATANUM_UINT64),
					(byte)((v & (long)(0xFF00000000000000L)) >>> 56),
					(byte)((v & (long)(0x00FF000000000000L)) >>> 48),
					(byte)((v & (long)(0x0000FF0000000000L)) >>> 40),
					(byte)((v & (long)(0x000000FF00000000L)) >>> 32),
					(byte)((v & (long)(0x00000000FF000000L)) >>> 24),
					(byte)((v & (long)(0x0000000000FF0000L)) >>> 16),
					(byte)((v & (long)(0x000000000000FF00L)) >>>  8),
					(byte)( v & (long)(0x00000000000000FFL))
				};
			}
		}
	}

	/**
	 * Returns the number extracted from the byte data
	 * @param buffer ByteBuffer
	 * @return long
	 */
	public static long intFromBytes(final ByteBuffer buffer) {
		int type = buffer.get();
		log.debug("Type = {}", type);
		switch(numericSubtype(type)) {
		case DATANUM_BOOLFALSE: return 0;
		case DATANUM_BOOLTRUE: return 1;
		case DATANUM_UINT8:
			log.debug("read uint8");
			return (long) (buffer.get() & (short)0xFF);
		case DATANUM_SINT8:
			log.debug("read sint8");
			return (long) buffer.get();
		case DATANUM_UINT16:
			log.debug("read uint16");
			return (long) (buffer.getShort() & 0xFFFF);
		case DATANUM_SINT16:
			log.debug("read sint16");
			return (long) buffer.getShort();
		case DATANUM_UINT32:
			log.debug("read uint32");
			return (long) (buffer.getInt() & 0xFFFFFFFFL);
		case DATANUM_SINT32:
			log.debug("read sint32");
			return (long) buffer.getInt();
		case DATANUM_UINT64:
			log.debug("read uint64");
			return (long) buffer.getLong();
		case DATANUM_SINT64:
			log.debug("read sint64");
			return (long) buffer.getLong();
		default:
			log.error("Unknown numeric type");
			break;
		}
		return 0;
	}

	/**
	 * Returns the byte data representation for the given bool
	 * @param v boolean
	 * @return byte[]
	 */
	public static byte[] bytesForBool(final boolean v) {
		return new byte[] {
			(byte)((DATA_NUMBER) | (v ? DATANUM_BOOLTRUE : DATANUM_BOOLFALSE))
		};
	}

	/**
	 * Returns true if the given data type information is the
	 * same number type as the type parameter
	 * @param v int
	 * @param type int
	 * @return boolean
	 */
	public static boolean validateNum(final int v, final int type) {
		return (v & 0xE0) == (DATA_NUMBER << 5) && (v & 0x1F) == type;
	}

	/**
	 * Method validateType.
	 * @param v int
	 * @param type int
	 * @return boolean
	 */
	public static boolean validateType(final int v, final int type) {
		return (v >> 5) == type;
	}

	/**
	 * Method numericSubtype.
	 * @param v int
	 * @return int
	 */
	public static int numericSubtype(final int v) {
		return (int)(v & 0x1F);
	}

	/**
	 * Returns the string extracted from the byte data
	 * @param buffer ByteBuffer
	 * @return boolean
	 */
	public static boolean boolFromBytes(final ByteBuffer buffer) {
		final int v = buffer.get() & (int)0xFF;
		final int subtype = numericSubtype(v);
		if(!validateNum(v, subtype)) {
			log.error("Not a boolean?");
		}
		return subtype == DATANUM_BOOLTRUE;
	}

	/**
	 * Returns the byte data representation for the given string
	 * @param v String
	 * @return byte[]
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] bytesForStr(final String v) throws IOException, UnsupportedEncodingException {
		final byte[] asUTF8 = v.getBytes("UTF-8");
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(asUTF8.length <= 30) {
			os.write((byte)((DATA_STRING << 5) | asUTF8.length));
		} else if(asUTF8.length <= 127) {
			os.write((byte)((DATA_STRING << 5) | 31));
			os.write((byte)(asUTF8.length));
		} else {
			os.write((byte)((DATA_STRING << 5) | 31));
			os.write((int)(0x80000000 | asUTF8.length));
		}
		os.write(asUTF8);
		return os.toByteArray();
	}

	/**
	 * Method sizeFromBuffer.
	 * @param type int
	 * @param buffer ByteBuffer
	 * @return int
	 */
	public static int sizeFromBuffer(int type, final ByteBuffer buffer) {
		if((type & 0x1F) != 0x1F) {
			return type & 0x1F;
		} else {
			int pos = buffer.position();
			int len = buffer.get();
			if((len & 0x80) != 0) {
				buffer.position(pos);
				return buffer.getInt() & 0x7FFFFFFF;
			} else {
				return len;
			}
		}
	}

	/**
	 * Returns the string extracted from the byte data
	 * @param buffer ByteBuffer
	 * @return String
	 */
	public static String strFromBytes(final ByteBuffer buffer) {
		int pos = buffer.position();
		final int v = buffer.get() & (int)0xFF;
		int size = sizeFromBuffer(v, buffer);
		if(size < 0) {
			log.error(String.format("Invalid size %d for buffer %s, v = %02x", size, buffer, v));
			buffer.position(pos);
			byte[] tmp = new byte[64];
			buffer.slice().get(tmp);
			log.debug(bytesToHex(tmp));
		}
		if(size == 0) {
			return "";
		}
		/* Probably dealing with a readonly buffer so we can't use .array() directly
		 * for string instantiation - there's doubtless a more efficient way to do this
		 */
		final byte[] copy = new byte[size];
		buffer.get(copy);
		final String str = new String(copy, 0, size, Charset.forName("UTF-8"));
		log.debug(">> String [{}]", str);
		return str;
	}

	/**
	 * Returns the byte data representation for the given string
	 * @param v String
	 * @return byte[]
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] bytesForList(final List<?> list, final Class cl) throws IOException, UnsupportedEncodingException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		int count = list.size();
		writeSize(os, DATA_LIST, count);
		if(cl == String.class) {
			writeListStr(os, (List<String>) list);
		} else if(cl == Integer.class) {
			writeListInt(os, (List<Integer>) list);
		} else {
			log.error("not sure what type we have, using object");
		}
		return os.toByteArray();
	}

	private static void writeListStr(final ByteArrayOutputStream os, final List<String> list) throws IOException {
		for(final String v : list) {
			os.write(bytesForStr(v));
		}
	}

	private static void writeListInt(final ByteArrayOutputStream os, final List<Integer> list) throws IOException {
		for(final Integer v : list) {
			os.write(bytesForInt(v));
		}
	}

	public static void writeSize(final ByteArrayOutputStream os, int type, int count) throws IOException {
		if(count <= 30) {
			os.write((byte)((type << 5) | count));
		} else if(count <= 127) {
			os.write((byte)((type << 5) | 31));
			os.write((byte)(count));
		} else {
			os.write((byte)((type << 5) | 31));
			os.write((int)(0x80000000 | count));
		}
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	/**
	 * Method bytesToHex.
	 * @param bytes byte[]
	 * @return String
	 */
	private static String bytesToHex(final byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}

