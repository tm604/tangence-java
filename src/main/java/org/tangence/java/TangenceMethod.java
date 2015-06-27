package org.tangence.java;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TangenceMethod {
	private List<String>			arguments = new ArrayList<String>();
	private String					returns;
	private	String					name;

	public TangenceMethod() { this.name = "unknown"; }

	public TangenceMethod returns(final String v) {
		this.returns = v;
		return this;
	}

	public List<String> arguments() { return arguments; }

	public TangenceMethod arguments(final List<String> args) {
		for(final String s : args) {
			this.arguments.add(s);
		}
		return this;
	}

	/**
	 * Method name.
	 * @return String
	 */
	public String name() { return name; }

	/**
	 * Method name.
	 * @param name String
	 * @return TangenceMethod
	 */
	public TangenceMethod name(final String name) { this.name = name; return this; }

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return "TangenceMethod[" + name + "]";
	}
}

