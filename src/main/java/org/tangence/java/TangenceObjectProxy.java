package org.tangence.java;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.bus.config.BusConfiguration;

import static org.tangence.java.Constants.*;

public class TangenceObjectProxy {
	private static Logger log = LoggerFactory.getLogger(TangenceObjectProxy.class.getName());

	private TangenceClass definition;
	private long id;
	private Map<String, Object> props = new HashMap<String, Object>();
	private Map<String, List<Runnable>> eventSubscriptions = new HashMap<String, List<Runnable>>();
	private MBassador<Bus.BusEvent> bus = new MBassador<Bus.BusEvent>(BusConfiguration.Default());

	private Connection conn;

	public TangenceObjectProxy(final TangenceClass definition) {
		this.definition = definition;
		log.debug(String.format("Instantiate [%s]", definition.name()));
	}

	public TangenceObjectProxy id(final long id) { this.id = id; return this; }
	public long id() { return id; }

	public TangenceObjectProxy connection(final Connection conn) {
		this.conn = conn;
		return this;
	}
	public Connection connection() { return conn; }

	public TangenceClass definition() { return definition; }

	public String classname() { return definition.name(); }

	public Future<TangenceMessage> call(final TangenceMethod m, final Object ... args) throws TangenceException {
		log.debug(String.format("Call method [%s] (class [%s])", m.name(), definition.name()));

		final TangenceMessageCall msg = new TangenceMessageCall(
			Constants.MSG_CALL,
			0,
			conn.registry()
		);
		msg.obj(this);
		msg.method(m);
		msg.arguments(args);
		conn.writeMessage(msg);
		return msg.completion();
	}

	public Future<TangenceMessage> call(final String methodname, final Object ... args) throws TangenceException {
		final TangenceMethod m = definition().getMethod(methodname);
		return call(m, args);
	}

	public void subscribe(final String name, final Runnable r) throws TangenceException {
		final TangenceEvent ev = definition().getEvent(name);
		subscribe(ev, r);
	}

	public void subscribe(final TangenceEvent ev, final Runnable r) throws TangenceException {
		if(definition == null) {
			log.error("Null definition");
		}
		if(ev == null) {
			log.error("Null event");
		}
		log.debug(String.format("Subscribe to event [%s] (class [%s])", ev.name(), definition.name()));
		List<Runnable> l = eventSubscriptions.get(ev.name());
		if(l == null) {
			l = new ArrayList<Runnable>();
			eventSubscriptions.put(ev.name(), l);

			final TangenceMessageSubscribe msg = new TangenceMessageSubscribe(
				Constants.MSG_SUBSCRIBE,
				0,
				conn.registry()
			);
			msg.obj(this);
			msg.event(ev);
			conn.writeMessage((byte)msg.type(), msg.payload());
		}
		l.add(r);
	}

	public void unsubscribe(final TangenceEvent ev) {
		log.debug(String.format("Unsubscribe from event [%s] (class [%s])", ev.name(), definition.name()));
	}

	public Future<TangenceMessage> watch(final String name, boolean wantInitial) throws TangenceException {
		final TangenceProperty prop = definition().getProperty(name);
		return watch(prop, wantInitial);
	}

	public Future<TangenceMessage> watch(final TangenceProperty prop, boolean wantInitial) throws TangenceException {
		if(definition == null) {
			log.error("Null definition");
		}
		if(prop == null) {
			log.error("Null event");
		}
		log.debug(String.format("Watch property [%s] (class [%s])", prop.name(), definition.name()));
		final TangenceMessageWatch msg = new TangenceMessageWatch(
			Constants.MSG_WATCH,
			0,
			conn.registry()
		);
		msg.obj(this);
		msg.property(prop);
		msg.wantInitial(wantInitial);
		conn.writeMessage((byte)msg.type(), msg.payload());
		return msg.completion();
	}

	public Future<TangenceMessage> getProperty(final TangenceProperty prop) throws TangenceException {
		log.debug(String.format("Get property [%s] (class [%s])", prop.name(), definition.name()));
		final TangenceMessageGetProp msg = new TangenceMessageGetProp(
			Constants.MSG_GETPROP,
			0,
			conn.registry()
		);
		msg.obj(this);
		msg.property(prop);
		conn.writeMessage(msg);
		return msg.completion();
	}

	public Future<TangenceMessage> getProperty(final String name) throws TangenceException {
		final TangenceProperty prop = definition().getProperty(name);
		return getProperty(prop);
	}


	public Future<Object> setProperty(final TangenceProperty prop, final Object v) {
		log.debug(String.format("Set property [%s] (class [%s])", prop.name(), definition.name()));
		return new Future<Object>();
	}

	public void onEvent(final String name, final Object ... args) {
		final List<Runnable> queue = eventSubscriptions.get(name);
		log.debug(String.format("Have %d callbacks for %s", queue.size(), name));
		for(final Runnable r : queue) {
			r.run();
		}
		log.debug("Finished callbacks");
	}

	public void applySmashed(final List<Object> param) {
		int idx = 0;
		for(final TangenceProperty prop : definition.smashed()) {
			final String name = prop.name();
			log.debug(String.format("Smashed property %s is %s", name, param.get(idx)));
			props.put(name, param.get(idx));
			++idx;
		}
	}

	public Object getSmashedPropertyValue(final String name) {
		return props.get(name);
	}

	public interface PropertyUpdate {
		public TangenceProperty property();
	}

	public class ScalarUpdate implements Bus.BusEvent, PropertyUpdate {
		private final TangenceProperty property;
		private final Object value;
		public ScalarUpdate(final TangenceProperty prop, final Object value) {
			this.property = prop;
			this.value = value;
		}
		public TangenceProperty property() { return property; }
		public Object value() { return value; }
	}

	public class HashUpdateSet implements Bus.BusEvent, PropertyUpdate {
		private final TangenceProperty property;
		private final Map<String, Object> value;
		public HashUpdateSet(final TangenceProperty prop, final Map<String, Object> value) {
			this.property = prop;
			this.value = value;
		}
		public TangenceProperty property() { return property; }
		public Map<String, Object> value() { return value; }
	}

	public class HashUpdateAdd implements Bus.BusEvent, PropertyUpdate {
		private final TangenceProperty property;
		private final String key;
		private final Object value;
		public HashUpdateAdd(final TangenceProperty prop, final String key, final Object value) {
			this.property = prop;
			this.key = key;
			this.value = value;
		}
		public TangenceProperty property() { return property; }
		public String key() { return key; }
		public Object value() { return value; }
	}

	public class HashUpdateRemove implements Bus.BusEvent, PropertyUpdate {
		private final TangenceProperty property;
		private final String key;
		public HashUpdateRemove(final TangenceProperty prop, final String key) {
			this.property = prop;
			this.key = key;
		}
		public TangenceProperty property() { return property; }
		public String key() { return key; }
	}

	public class QueueUpdateSet implements Bus.BusEvent, PropertyUpdate {
		private final TangenceProperty property;
		private final List<Object> value;
		public QueueUpdateSet(final TangenceProperty prop, final List<Object> value) {
			this.property = prop;
			this.value = value;
		}
		public TangenceProperty property() { return property; }
		public List<Object> value() { return value; }
	}

	public class QueueUpdatePush implements Bus.BusEvent, PropertyUpdate {
		private final TangenceProperty property;
		private final List<Object> value;
		public QueueUpdatePush(final TangenceProperty prop, final List<Object> value) {
			this.property = prop;
			this.value = value;
		}
		public TangenceProperty property() { return property; }
		public List<Object> value() { return value; }
	}

	public class QueueUpdateShift implements Bus.BusEvent, PropertyUpdate {
		private final TangenceProperty property;
		private final int count;
		public QueueUpdateShift(final TangenceProperty prop, final int count) {
			this.property = prop;
			this.count = count;
		}
		public TangenceProperty property() { return property; }
		public int count() { return count; }
	}

	public class ArrayUpdateSet extends QueueUpdateSet {
		public ArrayUpdateSet(final TangenceProperty prop, final List<Object> value) {
			super(prop, value);
		}
	}

	public class ArrayUpdatePush extends QueueUpdatePush {
		public ArrayUpdatePush(final TangenceProperty prop, final List<Object> value) {
			super(prop, value);
		}
	}

	public class ArrayUpdateShift extends QueueUpdateShift {
		public ArrayUpdateShift(final TangenceProperty prop, final int count) {
			super(prop, count);
		}
	}

	public void applyUpdate(final TangenceMessageUpdate m, final List<Object> param) {
		final String name = m.name();
		log.debug(String.format("We have a %d type update for %s on property %s", m.changeType(), this, name));
		final TangenceProperty prop = definition().getProperty(name);
		switch(prop.dimension()) {
		case DIM_SCALAR: {
			if(m.changeType() != CHANGE_SET) {
				log.error(String.format("Invalid change type for scalar, expected SET but had %d", m.changeType()));
			}
			if(param.size() != 1) {
				log.error(String.format("Invalid parameter list, expected 1 but had %d", param.size()));
			}
			final Object v = param.get(0);
			log.debug(String.format("Set scalar value to %s", v));
			props.put(name, v);
			bus.publish(new ScalarUpdate(prop, v));
			break;
		}
		case DIM_HASH: {
			switch(m.changeType()) {
			case CHANGE_SET: {
				final Map<String, Object> v = (Map<String, Object>) param.get(0);
				log.debug(String.format("Set hash to %s", v));
				props.put(name, v);
				bus.publish(new HashUpdateSet(prop, v));
				break;
			}
			case CHANGE_ADD: {
				final String k = (String) param.get(0);
				final Object v = param.get(1);
				log.debug(String.format("Add %s = %s", k, v));
				((Map<String, Object>)props.get(name)).put(k, v);
				bus.publish(new HashUpdateAdd(prop, k, v));
				break;
			}
			case CHANGE_DEL: {
				final String k = (String) param.get(0);
				log.debug(String.format("Remove %s", k));
				((Map<String, Object>)props.get(name)).remove(k);
				bus.publish(new HashUpdateRemove(prop, k));
				break;
			}
			default:
				log.error(String.format("Unknown change type %d, giving up", m.changeType()));
				break;
			}
			break;
		}
		case DIM_QUEUE: {
			switch(m.changeType()) {
			case CHANGE_SET: {
				final List<Object> v = (List<Object>) param.get(0);
				log.debug(String.format("Set queue to %s", v));
				props.put(name, v);
				bus.publish(new QueueUpdateSet(prop, v));
				break;
			}
			case CHANGE_PUSH: {
				final List<Object> v = param;
				log.debug(String.format("Push %s", v));
				((List<Object>)props.get(name)).addAll(v);
				bus.publish(new QueueUpdatePush(prop, v));
				break;
			}
			case CHANGE_SHIFT: {
				final int count = ((Long) param.get(0)).intValue();
				log.debug(String.format("Shift %d items", count));
				final List<Object> queue = (List<Object>)props.get(name);
				for(int i = 0; i < count; ++i)
					queue.remove(0);
				bus.publish(new QueueUpdateShift(prop, count));
				break;
			}
			default:
				log.error(String.format("Unknown change type %d, giving up", m.changeType()));
				break;
			}
			break;
		}
		case DIM_ARRAY: {
			switch(m.changeType()) {
			case CHANGE_SET: {
				final List<Object> v = (List<Object>) param.get(0);
				log.debug(String.format("Set queue to %s", v));
				props.put(name, v);
				bus.publish(new ArrayUpdateSet(prop, v));
				break;
			}
			case CHANGE_PUSH: {
				final List<Object> v = param;
				log.debug(String.format("Push %s", v));
				((List<Object>)props.get(name)).addAll(v);
				bus.publish(new ArrayUpdatePush(prop, v));
				break;
			}
			case CHANGE_SHIFT: {
				final int count = ((Long) param.get(0)).intValue();
				log.debug(String.format("Shift %d items", count));
				final List<Object> queue = (List<Object>)props.get(name);
				for(int i = 0; i < count; ++i)
					queue.remove(0);
				bus.publish(new ArrayUpdateShift(prop, count));
				break;
			}
			default:
				log.error(String.format("Unknown change type %d, giving up", m.changeType()));
				break;
			}
			break;
		}
		default:
			log.debug(String.format("Unknown property dimension %d, giving up", prop.dimension()));
			break;
		}
	}

	/**
	 * Provide access to our message bus.
	 * Currently goes through the singleton in {@link Bus}, this
	 * should change in future.
	 */
	public MBassador<Bus.BusEvent> bus() { return bus; }
}

