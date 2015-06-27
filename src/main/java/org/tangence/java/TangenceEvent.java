package org.tangence.java;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TangenceEvent {
	private List<String>			arguments = new ArrayList<String>();
	private	String					name;

	public TangenceEvent() { this.name = "unknown"; }

	public TangenceEvent arguments(final List<String> args) {
		for(final String s : args) {
			this.arguments.add(s);
		}
		return this;
	}

	public List<String> arguments() { return arguments; }

	/**
	 * Method name.
	 * @return String
	 */
	public String name() { return name; }

	/**
	 * Method name.
	 * @param name String
	 * @return TangenceEvent
	 */
	public TangenceEvent name(final String name) { this.name = name; return this; }

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return "TangenceEvent[" + name + "]";
	}
}

