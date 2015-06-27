package org.tangence.java;

import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

public class Bus {
	private static final MBassador<BusEvent> bus = new MBassador<BusEvent>(BusConfiguration.Default());

	public interface BusEvent {
	}

	private Bus() { }

	public static final MBassador<BusEvent> bus() { return bus; }
}
