package org.tangence.java.gen;

/* Event bus used for posting Tangence events */
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

/* Logging abstraction */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Components from core Tangence implementation */
import org.tangence.java.Future;
import org.tangence.java.TangenceEvent;
import org.tangence.java.TangenceObjectProxy;

public class Example {
	/** Logging object, provides the .debug / .warning / .error methods */
	protected static final Logger log = LoggerFactory.getLogger(Example.class.getName());
	/** Event bus used for posting Tangence events back to ourselves and any interested listeners */
	private final MBassador<Event> bus = new MBassador<Event>(BusConfiguration.Default());
	/** The Tangence object proxy used for communicating with the remote */
	private final TangenceObjectProxy proxy;

	/** Base event definition */
	public class Event {
		private TangenceEvent definition;
		public Event(final TangenceEvent ev) {
			this.definition = ev;
		}
		public final TangenceEvent definition() { return definition; }
	}

// Class-specific events
	public class BouncedEvent extends Event {
		final String howhigh;

		public BouncedEvent(
			final TangenceEvent ev,
			final String howhigh
		) {
			super(ev);
			this.howhigh = howhigh;
		}

		public final String howhigh() { return howhigh; }
	}

	/**
	 * Constructor.
	 * Applies the {@link TangenceObjectProxy} definition and subscribes
	 * this class to the event bus so that any handlers on subclasses
	 * will be registered.
	 */
	public Example(final TangenceObjectProxy proxy) {
		this.proxy = proxy;
		bus.subscribe(this);
	}

	public void hadEvent(final Event ev) {
		log.debug("Posting message");
		bus.publish(ev);
		log.debug("Message done");
	}

	public void eventFactory(final String name, final Object ... args) {
		final TangenceEvent def = proxy.definition().getEvent(name);
		Event ev = null;
		switch(name) {
		case "bounced":
			ev = new BouncedEvent(def,
				(String) args[0]
			);
			break;
		default:
			log.error("Invalid name: " + name);
			break;
		}
		hadEvent(ev);
	}
}
