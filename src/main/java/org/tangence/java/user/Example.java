package org.tangence.java.user;

import net.engio.mbassy.listener.Handler;
import org.tangence.java.TangenceObjectProxy;

public class Example extends org.tangence.java.gen.Example {

// Main constructor
	public Example(final TangenceObjectProxy proxy) {
		super(proxy);
	}

	@Handler
	public void onBounced(final BouncedEvent ev) {
		log.debug("Bounced: " + ev.howhigh());
	}
}

