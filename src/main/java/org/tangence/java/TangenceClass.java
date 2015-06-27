package org.tangence.java;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangenceClass {
	protected static Logger log = LoggerFactory.getLogger(TangenceMessage.class.getName());

	private List<TangenceMethod>	methods;
	private List<TangenceEvent>		events;
	private List<TangenceProperty>	properties;
	private List<TangenceClass>		superclasses;
	private List<TangenceProperty>	smashed;
	private	String					name;

	private Map<String, TangenceProperty> propertyByName;
	private Map<String, TangenceEvent> eventByName;
	private Map<String, TangenceMethod> methodByName;

	public TangenceClass() {
		this.name = "unknown";
		this.methods = new ArrayList<TangenceMethod>();
		this.events = new ArrayList<TangenceEvent>();
		this.properties = new ArrayList<TangenceProperty>();
		this.superclasses = new ArrayList<TangenceClass>();
		this.propertyByName = new HashMap<String, TangenceProperty>();
		this.eventByName = new HashMap<String, TangenceEvent>();
		this.methodByName = new HashMap<String, TangenceMethod>();
		this.smashed = new ArrayList<TangenceProperty>();
	}

	/**
	 * Method name.
	 * @return String
	 */
	public String name() { return name; }

	/**
	 * Method name.
	 * @param name String
	 * @return TangenceClass
	 */
	public TangenceClass name(final String name) { this.name = name; return this; }

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return "TangenceClass[" + name + "]";
	}

	/**
	 * Method addMethod.
	 * @param method TangenceMethod
	
	 * @return TangenceClass */
	public TangenceClass addMethod(final TangenceMethod method) {
		methods.add(method);
		methodByName.put(method.name(), method);
		return this;
	}

	public TangenceMethod getMethod(final String name) {
		return methodByName.get(name);
	}

	/**
	 * Method addEvent.
	 * @param event TangenceEvent
	
	 * @return TangenceClass */
	public TangenceClass addEvent(final TangenceEvent event) {
		events.add(event);
		eventByName.put(event.name(), event);
		return this;
	}

	public TangenceEvent getEvent(final String name) {
		return eventByName.get(name);
	}

	/**
	 * Method addProperty.
	 * @param property TangenceProperty
	
	 * @return TangenceClass */
	public TangenceClass addProperty(final TangenceProperty property) {
		properties.add(property);
		propertyByName.put(property.name(), property);
		return this;
	}

	public TangenceProperty getProperty(final String name) {
		return propertyByName.get(name);
	}

	/**
	 * Method addSuperclass.
	 * @param superclass TangenceClass
	
	 * @return TangenceClass */
	public TangenceClass addSuperclass(final TangenceClass superclass) {
		superclasses.add(superclass);
		return this;
	}

	/**
	 * Method hasSmashed.
	 * @return boolean
	 */
	public boolean hasSmashed() { return !smashed.isEmpty(); }

	public TangenceObjectProxy instantiate() {
		final TangenceObjectProxy obj = new TangenceObjectProxy(
			this
		);
		return obj;
	}

	public List<TangenceProperty> smashed() { return smashed; }
	public TangenceClass smashed(final List<String> smash) {
		for(final String name : smash) {
			smashed.add(getProperty(name));
		}
		return this;
	}

	public List<TangenceMethod>	methods() { return methods; }
	public List<TangenceEvent> events() { return events; }
	public List<TangenceProperty> properties() { return properties; }

	/**
	 * Returns true if this class inherits from the given name.
	 */
	public boolean isa(final String name) {
		if(this.name.equals(name))
			return true;

		for(final TangenceClass ancestor : superclasses) {
			log.debug(String.format("Checking whether %s isa %s - %s", this.name, name, ancestor.name()));
			if(ancestor.name().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean isa(final TangenceClass ancestor) {
		return isa(ancestor.name());
	}
}

