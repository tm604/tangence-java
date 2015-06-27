package org.tangence.java;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.tangence.java.Constants.*;

public class MessageBufferFactory {
	private static Logger log = LoggerFactory.getLogger(MessageBufferFactory.class.getName());

	private Registry registry;

	public MessageBufferFactory(final Registry registry) {
		this.registry = registry;
	}

	public Registry registry() { return registry; }

	/**
	 * Constructs an object.
	 */
	public void construct(final ByteBuffer buffer) {
		final Long objectID = Types.intFromBytes(buffer);
		final Long classID = Types.intFromBytes(buffer);
		log.debug(String.format("Look for class id %s for object ID %s", classID, objectID));
		final TangenceClass c = registry.classFromID(classID);
		log.debug(String.format("Object will be of class %s", c.name()));

		final TangenceObjectProxy o = c.instantiate();

		final Object smashed = listFromBytes(buffer);
		if(c.hasSmashed()) {
			log.debug("We have smashed properties to apply");
			o.applySmashed((List<Object>)smashed);
		} else {
			log.debug("No smashed props here");
		}
		registry.addObject(objectID, o);
	}

	/**
	 * Adds a class definition.
	 */
	public void addClass(final ByteBuffer buffer) {
		final String name = Types.strFromBytes(buffer);
		final Long id = Types.intFromBytes(buffer);
		log.debug(String.format("Adding class %s (id %s)", name, id));
		final TangenceClass o = (TangenceClass)recordFromBytes(buffer);
		o.name(name);
		registry.addClass(id, o);
	}

	/**
	 * Returns the byte data representation for the given thing.
	 * FIXME not yet implemented
	 * @param v Object
	 * @return byte[]
	 */
	public byte[] bytesForAny(final Object v) {
		return new byte[] { };
	}

	/**
	 * Method dictFromBytes.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public Object dictFromBytes(final ByteBuffer buffer) {
		final int v = buffer.get() & (int)0xFF;
		int count = Types.sizeFromBuffer(v, buffer);
		final Map<String, Object> m = new HashMap<String, Object>();
		for(int i = 0; i < count; ++i) {
			final String k = Types.strFromBytes(buffer);
			final Object o = anyFromBytes(buffer);
			log.debug("* {} => {}", k, o);
			m.put(k, o);
		}
		return m;
	}

	/**
	 * Method listFromBytes.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public Object listFromBytes(final ByteBuffer buffer) {
		final int v = buffer.get() & (int)0xFF;
		int count = Types.sizeFromBuffer(v, buffer);
		final List<Object> m = new ArrayList<Object>();
		for(int i = 0; i < count; ++i) {
			final Object o = anyFromBytes(buffer);
			m.add(o);
		}
		return m;
	}

	public <T> List<T> listFrom(final ByteBuffer buffer) {
		final int v = buffer.get() & (int)0xFF;
		int count = Types.sizeFromBuffer(v, buffer);
		final List<T> m = new ArrayList<T>();
		for(int i = 0; i < count; ++i) {
			final T o = (T)anyFromBytes(buffer);
			m.add(o);
		}
		return m;
	}

	/**
	 * Method objFromBytes.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public Object objFromBytes(final ByteBuffer buffer) {
		final int v = buffer.get() & (int)0xFF;
		log.debug(String.format("Obj:: %d", v));
		int size = Types.sizeFromBuffer(v, buffer);
		if(size == 0) {
			log.error("Zero size, this is unusual for an object");
			return null;
		}
		if(size != 4) {
			log.error("Unusual object ID size: {}", size);
		}
		final Long id = new Integer(buffer.getInt()).longValue();
		log.debug(String.format("Object ID is %d from a size of %d", id, size));
		return registry.getObject(id);
	}

	/**
	 * Method recordFromBytes.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public Object recordFromBytes(final ByteBuffer buffer) {
		final int v = buffer.get() & (int)0xFF;
		final int count = Types.sizeFromBuffer(v, buffer);
		final int id = (int)Types.intFromBytes(buffer);
		log.debug("Record for class [{}]", id);
		if(id == 1) {
			return getTangenceClass(buffer);
		} else if(id == 2) {
			return getTangenceMethod(buffer);
		} else if(id == 3) {
			return getTangenceEvent(buffer);
		} else if(id == 4) {
			return getTangenceProperty(buffer);
		} else {
			final List<Object> m = new ArrayList<Object>();
			for(int i = 0; i < count; ++i) {
				final Object o = anyFromBytes(buffer);
				m.add(o);
			}
			return m;
		}
	}

	/**
	 * Returns the thing extracted from the byte data
	 * @param buffer ByteBuffer
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public Object anyFromBytes(final ByteBuffer buffer) {
		/* Could have several meta records to set things up for the
		 * actual data that needs processing, so we have this outer
		 * loop to keep dealing with the next item until we get
		 * something we can return.
		 */
		while(true) {
			/* Non-destructive read for the type info */
			int pos = buffer.position();
			final int type = buffer.get();
			buffer.position(pos);
			switch((type & 0xE0) >> 5) {
			case DATA_NUMBER:
				log.debug("We have a number");
				return Types.intFromBytes(buffer);
			case DATA_STRING:
				final String s = Types.strFromBytes(buffer);
				log.debug("We have a string: {}", s);
				return s;
			case DATA_LIST:
				log.debug("We have a list");
				return listFromBytes(buffer);
			case DATA_DICT:
				log.debug("We have a dict");
				return dictFromBytes(buffer);
			case DATA_OBJECT:
				log.debug("We have an object");
				return objFromBytes(buffer);
			case DATA_RECORD:
				log.debug("We have a record");
				return recordFromBytes(buffer);
			case DATA_META:
				log.debug("We have meta");
				metaFromBytes(buffer);
				break;
			default:
				log.debug("We have something else");
				break;
			}
		}
	}

	/**
	 * Method metaFromBytes.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public void metaFromBytes(final ByteBuffer buffer) {
		final int type = buffer.get();
		final int subtype = type & 0x0F;
		switch(subtype) {
		case DATAMETA_CONSTRUCT:
			construct(buffer);
			break;
		case DATAMETA_CLASS:
			addClass(buffer);
			break;
		case DATAMETA_STRUCT:
			log.debug("Struct");
			break;
		default:
			log.debug("Unknown meta");
			break;
		}
	}

	/**
	 * Method getTangenceClass.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public TangenceClass getTangenceClass(final ByteBuffer buffer) {
		final Map<String, TangenceMethod> methods = (Map<String, TangenceMethod>) dictFromBytes(buffer);
		final Map<String, TangenceEvent> events = (Map<String, TangenceEvent>) dictFromBytes(buffer);
		final Map<String, TangenceProperty> properties = (Map<String, TangenceProperty>) dictFromBytes(buffer);
		final List<String> superclasses = (List<String>) listFromBytes(buffer);
		final List<String> smashed = (List<String>) listFromBytes(buffer);
		final Map<String, Boolean> smashedMap = new HashMap<String, Boolean>();
		for(final String s : smashed) {
			log.debug(String.format("Marking %s as smashed", s));
			smashedMap.put(s, true);
		}

		log.debug("*** Extracting class info");
		final TangenceClass cl = new TangenceClass();
		for(final String parent : superclasses) {
			log.debug(String.format("* Merging parent class information from %s", parent));
			final TangenceClass spec = registry.classFromName(parent);
			for(final TangenceMethod m : spec.methods()) {
				methods.put(m.name(), m);
			}
			for(final TangenceProperty p : spec.properties()) {
				properties.put(p.name(), p);
			}
			for(final TangenceEvent e : spec.events()) {
				events.put(e.name(), e);
			}
		}
		for(final String k : methods.keySet()) {
			final TangenceMethod m = methods.get(k);
			m.name(k);
			log.debug("* Method [{}] => {}", k, m.toString());
			cl.addMethod(m);
		}
		for(final String k : events.keySet()) {
			final TangenceEvent ev = events.get(k);
			ev.name(k);
			log.debug("* Event [{}] => {}", k, ev.toString());
			cl.addEvent(ev);
		}
		for(final String k : properties.keySet()) {
			final TangenceProperty p = properties.get(k);
			p.name(k);
			if(smashedMap.containsKey(k)) {
				log.debug(String.format("... and %s looks smashed", k));
				p.smashed(true);
			}
			log.debug("* Property [{}] => {}", k, p.toString());
			cl.addProperty(p);
		}
		for(final String k : superclasses) {
			log.debug("* ISA [{}]", k);
			cl.addSuperclass(registry.classFromName(k));
		}
		cl.smashed(smashed);
		return cl;
	}

	/**
	 * Method getTangenceMethod.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public TangenceMethod getTangenceMethod(final ByteBuffer buffer) {
		final List<String> arguments = listFrom(buffer);
		final String returns = Types.strFromBytes(buffer);
		final TangenceMethod m = new TangenceMethod();
		return m.returns(returns).arguments(arguments);
	}

	/**
	 * Method getTangenceEvent.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public TangenceEvent getTangenceEvent(final ByteBuffer buffer) {
		final List<String> arguments = listFrom(buffer);
		final TangenceEvent ev = new TangenceEvent();
		return ev.arguments(arguments);
	}

	/**
	 * Method getTangenceProperty.
	 * @param buffer ByteBuffer
	 * @return Object
	 */
	public TangenceProperty getTangenceProperty(final ByteBuffer buffer) {
		int dimension = (int)Types.intFromBytes(buffer);
		String type = Types.strFromBytes(buffer);
		boolean smashed = Types.boolFromBytes(buffer);
		final TangenceProperty prop = new TangenceProperty();
		return prop.dimension(dimension).type(type).smashed(smashed);
	}
}

