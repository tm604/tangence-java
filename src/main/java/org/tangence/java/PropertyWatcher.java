package org.tangence.java;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyWatcher {
	private static Logger log = LoggerFactory.getLogger(PropertyWatcher.class.getName());

	public PropertyWatcher() { }

	public void dispatch(final TangenceObjectProxy.ScalarUpdate ev) {
		log.debug("scalar update");
		final Object v = ev.value();
		log.debug(String.format("scalar value = %s", v));
		valueChanged(v);
		log.debug("done pw update");
	}

	public void valueChanged(Object o) {
		log.debug(String.format("default valueChanged handler picked up %s", o));
	}

	public void dispatch(final TangenceObjectProxy.HashUpdateSet ev) {
		log.debug("hash set");
		final Map<String, Object> hash = ev.value();
		for(final String k : hash.keySet()) {
			keyAdded(k, hash.get(k));
		}
	}

	public void dispatch(final TangenceObjectProxy.HashUpdateAdd ev) {
		log.debug("hash add");
		keyAdded(ev.key(), ev.value());
	}
	public void dispatch(final TangenceObjectProxy.HashUpdateRemove ev) {
		log.debug("hash remove");
		keyRemoved(ev.key());
	}

	public void keyAdded(final String k, final Object v) { }
	public void keyRemoved(final String k) { }

	public void dispatch(final TangenceObjectProxy.QueueUpdateSet ev) {
		log.debug("queue set");
		final List<Object> list = ev.value();
		for(final Object v : list) {
			itemPushed(v);
		}
	}

	public void dispatch(final TangenceObjectProxy.QueueUpdatePush ev) {
		log.debug("queue push");
		final List<Object> list = ev.value();
		for(final Object v : list) {
			itemPushed(v);
		}
	}

	public void dispatch(final TangenceObjectProxy.QueueUpdateShift ev) {
		log.debug("queue push");
		for(int idx = 0; idx < ev.count(); ++idx) {
			itemShifted();
		}
	}

	public void dispatch(final TangenceObjectProxy.ArrayUpdateSet ev) {
		log.debug("array set");
		final List<Object> list = ev.value();
		for(final Object v : list) {
			itemPushed(v);
		}
	}

	public void dispatch(final TangenceObjectProxy.ArrayUpdatePush ev) {
		log.debug("array push");
		final List<Object> list = ev.value();
		for(final Object v : list) {
			itemPushed(v);
		}
	}

	public void dispatch(final TangenceObjectProxy.ArrayUpdateShift ev) {
		log.debug("array push");
		for(int idx = 0; idx < ev.count(); ++idx) {
			itemShifted();
		}
	}

	public void itemPushed(final Object v) { }
	public void itemShifted() { }
}
