package org.tangence.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds all objects and classes.
 */
public class Registry {
	private static final Logger log = LoggerFactory.getLogger(Registry.class.getName());

	private Map<Long, TangenceObjectProxy> objects = new HashMap<Long, TangenceObjectProxy>();
	private List<TangenceClass> classes = new ArrayList<TangenceClass>();
	private List<Object> structs = new ArrayList<Object>();

	private Map<Long, TangenceClass> classByID = new HashMap<Long, TangenceClass>();
	private Map<String, TangenceClass> classByName = new HashMap<String, TangenceClass>();

	private Connection conn;

	public Registry() {
	}

	/**
	 * Method addClass.
	 * @param id long
	 * @param c TangenceClass
	 */
	public Registry addClass(final Long id, final TangenceClass c) {
		log.debug(String.format("Adding class %s (%s)", id, c.name()));
		classes.add(c);
		classByID.put(id, c);
		classByName.put(c.name(), c);
		return this;
	}

	/**
	 * Method classFromID.
	 * @param id long
	 * @return TangenceClass
	 */
	public TangenceClass classFromID(final Long id) { return classByID.get(id); }

	/**
	 * Method classFromName.
	 * @param name String
	 * @return TangenceClass
	 */
	public TangenceClass classFromName(final String name) { return classByName.get(name); }

	public Registry addObject(final Long id, final TangenceObjectProxy o) {
		log.debug(String.format("Adding object %s", id));
		o.connection(conn);
		o.id(id);
		if(objects.containsKey(id))
			log.error(String.format("Already found %s in the registry?", id));
		objects.put(id, o);
		return this;
	}

	public TangenceObjectProxy getObject(final Long id) {
		if(!objects.containsKey(id))
			log.error(String.format("We were asked for %d but did not find it", id));
		return objects.get(id);
	}

	public Registry connection(final Connection conn) { this.conn = conn; return this; }
	public Connection connection() { return conn; }
}

