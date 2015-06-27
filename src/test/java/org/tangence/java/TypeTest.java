package org.tangence.java;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

import org.tap4j.ext.junit.runner.TapRunnerClass;

import static org.tangence.java.TAP.*;

@RunWith(TapRunnerClass.class)
public class TypeTest
{
	/**
	 * Check roundtrip for strings.
	 */
	@Test
    public void testBoolEncoding()
    {
		ok(!Types.boolFromBytes(ByteBuffer.wrap(Types.bytesForBool(false))), "Check false can roundtrip successfully");
		ok(Types.boolFromBytes(ByteBuffer.wrap(Types.bytesForBool(true))), "Check false can roundtrip successfully");
    }

    /**
	 * Check roundtrip for integers.
     */
	@Test
    public void testIntEncoding()
    {
		final List<Long> values = new ArrayList<Long>() {{
			/** I love java. org.apache.commons.lang.ArrayUtils would help here */
			add((Long)(0L));
			add((Long)(1L));
			add((Long)(2L));
			add((Long)(-1L));
			add((Long)(123L));
			add((Long)(127L));
			add((Long)(128L));
			add((Long)(254L));
			add((Long)(255L));
			add((Long)(-127L));
			add((Long)(-128L));
			add((Long)(-255L));
			add((Long)(-256L));
			add((Long)(257L));
			add((Long)(65535L));
			add((Long)(32767L));
			add((Long)(32768L));
			add((Long)(-32767L));
			add((Long)(4194304L));
			add((Long)(-4194304L));
			add((Long)(4294967295L));
			add((Long)(-4294967295L));
			add((Long)(4494967295L));
			add((Long)(-4494967295L));
		}};
		for(long v : values) {
			final byte[] asBytes = Types.bytesForInt(v);
			is(
				Types.intFromBytes(ByteBuffer.wrap(asBytes)),
				v,
				"Check that " + String.valueOf(v) + " can roundtrip successfully"
			);
		}
    }

	/**
	 * Check roundtrip for strings.
	 */
	@Test
    public void testStrEncoding()
    {
		try {
			final List<String> values = Arrays.asList(
				"", "Test", "test", "abc", "ünûßàł",
				"something with spaces",
				"something with more text than before",
				"text\nwith\nnew\nlines",
				"obligatory \" \000 characters"
			);
			for(final String v : values) {
				is(v, Types.strFromBytes(ByteBuffer.wrap(Types.bytesForStr(v))), "Check that " + String.valueOf(v) + " can roundtrip successfully");
			}
		} catch(IOException e) {
			fail("Had exception: " + e.getMessage());
		}
    }

}

