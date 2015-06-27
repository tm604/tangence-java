package org.tangence.java;

import org.junit.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TAP {
	private static Logger log = LoggerFactory.getLogger(TAP.class.getName());

	/**
	 * Simple comparison via assertEquals - check whether the first parameter == second parameter
	 */
	public static <T> boolean is(final T actual, final T expected, final String message) {
		final boolean rslt = actual.equals(expected);
		log.info((rslt ? "ok" : "not ok") + " - " + sanitise(message));
		if(!rslt) {
			log.info("expected: {}", expected);
			log.info("  actual: {}", actual);
		}
		Assert.assertEquals(message, actual, expected);
		return rslt;
	}

	public static boolean ok(final boolean rslt, final String message) {
		log.info((rslt ? "ok" : "not ok") + " - " + sanitise(message));
		Assert.assertTrue(message, rslt);
		return rslt;
	}

	public static boolean fail(final String message) {
		log.info("not ok - " + sanitise(message));
		Assert.fail(message);
		return false;
	}

	public static boolean pass(final String message) {
		log.info("ok - " + sanitise(message));
		Assert.assertTrue(message, true);
		return false;
	}

	public static void note(final String message) {
		log.info("# " + sanitise(message));
	}

	private static String sanitise(final String message) {
		if(message.indexOf("\n") < 0) return message;

		final StringBuilder sb = new StringBuilder();
		for(final String line : message.split("\n")) {
			if(sb.length() > 0) {
				sb.append("\n# " + line);
			} else {
				sb.append(line);
			}
		}
		return sb.toString();
	}
}
