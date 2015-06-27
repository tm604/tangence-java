#!/usr/bin/env perl 
use strict;
use warnings;
use Dir::Self;
use Template;
use 5.010;
use Tangence::Compiler::Parser;
use Tangence::Constants;

my $factory = <<'EOF';
package [% pkg %].gen;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tangence.java.TangenceObjectProxy;

public class Factory {
	private static final Logger log = LoggerFactory.getLogger(Factory.class.getName());

	/**
	 * Returns an appropriate façade for the given proxy object.
	 */
	public static Object getObject(final TangenceObjectProxy proxy) {
		try {
			final String name = proxy.definition().name().replace(".", "");
			log.info("Looking for Tangence class " + name);
			Class c;
			try {
				c = Class.forName("[% pkg %].user." + name);
			} catch(final ClassNotFoundException e) {
				log.info("No user class found, falling back to generated default");
				c = Class.forName("[% pkg %].gen." + name);
			}
			log.info("Now have class " + c);
			return c.getConstructor(TangenceObjectProxy.class).newInstance(proxy);
		} catch(final ClassNotFoundException e) {
			log.error("did not find generated class - " + e.getMessage());
		} catch(final NoSuchMethodException e) {
			log.error("method was missing - " + e.getMessage());
		} catch(final InstantiationException e) {
			log.error("construction failed - " + e.getMessage());
		} catch(final IllegalAccessException e) {
			log.error("access failed - " + e.getMessage());
		} catch(final InvocationTargetException e) {
			log.error("invocation failed", e.getTargetException());
		}
		return null;
	}
}

EOF

my $interface = <<'EOF';
package [% pkg %].tan;

/* Collections */
import java.util.List;
import java.util.Map;
import java.util.Set;

/* Components from core Tangence implementation */
import org.tangence.java.Future;
import org.tangence.java.TangenceException;
import org.tangence.java.TangenceClass;
import org.tangence.java.TangenceEvent;
import org.tangence.java.TangenceObjectProxy;
import org.tangence.java.PropertyWatcher;

public interface [% classname %] {
	public boolean isa(final String ancestor);
	public boolean isa(final TangenceClass ancestor);

// Property accessors
[% FOREACH prop IN properties -%]
[%  IF prop.smashed -%]
	/**
	 * [% prop.name %] accessor. This is a smashed property so the value is always
	 * available immediately, and any changes will propagate directly to the server.
	 */
	public [% prop.java_aggregate_type %] [% prop.name %]();
	public Future [% prop.name %](final [% prop.java_aggregate_type %] [% prop.name %]);
[%  ELSE -%]
	/**
	 * [% prop.name %] accessor. This is a regular property so we return a {@link Future}
	 * representing the value on completion.
	 */
	public Future [% prop.name %]() throws TangenceException;
	public Future [% prop.name %](final [% prop.java_aggregate_type %] [% prop.name %]);
[%  END -%]
	public void [% prop.name %]_watch(PropertyWatcher pw, boolean wantInitial) throws TangenceException;
[% END -%]

// Methods
[% FOREACH method IN methods -%]
	/**
	 */
	public Future [% method.name %](
[%  FOREACH arg IN method.args -%]
		final [% arg.type %] [% arg.name %][% loop.last ? '' : ',' %]
[%  END -%]
	) throws TangenceException;
[% END -%]
}
EOF

my $implementation = <<'EOF';
package [% pkg %].gen;

/* Collections */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* Event bus used for posting Tangence events */
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

/* Logging abstraction */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Components from core Tangence implementation */
import org.tangence.java.Future;
import org.tangence.java.Bus;
import org.tangence.java.PropertyWatcher;
import org.tangence.java.TangenceClass;
import org.tangence.java.TangenceException;
import org.tangence.java.TangenceEvent;
import org.tangence.java.TangenceProperty;
import org.tangence.java.TangenceObjectProxy;

public class [% classname %]
implements [%
interface_list = super;
interface_list.push(classname);
%]
[% FOREACH parent IN interface_list -%]
	[% pkg _ '.tan.' _ parent %][% loop.last ? '' : ', ' %]
[% END -%]
{
	/** Logging object, provides the .debug / .warning / .error methods */
	protected static final Logger log = LoggerFactory.getLogger([% classname %].class.getName());
	/** Event bus used for posting Tangence events back to ourselves and any interested listeners */
	private final MBassador<Bus.BusEvent> bus;
	/** The Tangence object proxy used for communicating with the remote */
	private final TangenceObjectProxy proxy;

// Class-specific properties
[% FOREACH prop IN properties -%]
	protected [% prop.java_aggregate_type %] [% prop.name %];
[% END -%]

	private final Map<String, List<PropertyWatcher>> watchers = new HashMap<String, List<PropertyWatcher>>();

	/** Base event definition */
	public class Event implements Bus.BusEvent {
		private TangenceEvent definition;
		public Event(final TangenceEvent ev) {
			this.definition = ev;
		}
		public final TangenceEvent definition() { return definition; }
	}

// Class-specific events
[% FOREACH ev IN events -%]
	public class [% ev.classname %] extends Event {
[%  FOREACH arg IN ev.args -%]
		final [% arg.type %] [% arg.name %];
[%  END -%]

		public [% ev.classname %](
			final TangenceEvent ev[% IF ev.args.size %],[% END %]
[%  FOREACH arg IN ev.args -%]
			final [% arg.type %] [% arg.name %][% loop.last ? '' : ',' %]
[%  END -%]
		) {
			super(ev);
[%  FOREACH arg IN ev.args -%]
			this.[% arg.name %] = [% arg.name %];
[%  END -%]
		}

[%  FOREACH arg IN ev.args -%]
		public final [% arg.type %] [% arg.name %]() { return [% arg.name %]; }
[%  END -%]
	}
[% END -%]

	/**
	 * Constructor.
	 * Applies the {@link TangenceObjectProxy} definition and subscribes
	 * this class to the event bus so that any handlers on subclasses
	 * will be registered.
	 */
	public [% classname %](final TangenceObjectProxy proxy) {
		this.proxy = proxy;
		this.bus = proxy.bus();
[% FOREACH prop IN properties -%]
[%  IF prop.is_list -%]
		this.[% prop.name %] = new Array[% prop.java_aggregate_type %]();
[%  ELSIF prop.is_hash -%]
		this.[% prop.name %] = new Hash[% prop.java_aggregate_type %]();
[%  ELSIF prop.is_objset -%]
		this.[% prop.name %] = new Hash[% prop.java_aggregate_type %]();
[%  ELSE -%]
		this.[% prop.name %] = null;
[%  END -%]
[% END -%]
		applySmashed();
		bus.subscribe(this);
	}

	private void applySmashed() {
		for(final TangenceProperty prop : proxy.definition().smashed()) {
			final String name = prop.name();
			final Object value = proxy.getSmashedPropertyValue(name);
			switch(name) {
[% FOREACH prop IN properties -%]
[%  IF prop.smashed -%]
			case "[% prop.name %]":
				log.debug(String.format("Smashing %s for %s", name, value));
[%   IF prop.java_aggregate_type == 'Boolean' -%]
				if(value == null) {
					this.[% prop.name %] = null;
				} else if(value.getClass() == Long.class) {
					this.[% prop.name %] = ((Long) value) != 0;
				} else if(value.getClass() == Integer.class) {
					this.[% prop.name %] = ((Integer) value) != 0;
				} else {
					this.[% prop.name %] = ([% prop.java_aggregate_type %]) value;
				}
[%   ELSIF prop.java_aggregate_type == 'String' -%]
				if(value.getClass() == [% prop.java_aggregate_type %].class) {
					this.[% prop.name %] = ([% prop.java_aggregate_type %]) value;
				} else {
					this.[% prop.name %] = [% prop.java_aggregate_type %].valueOf(value);
				}
[%   ELSE -%]
				this.[% prop.name %] = ([% prop.java_aggregate_type %]) value;
[%   END -%]
				break;
[%  END -%]
[% END -%]
			default:
				log.error("Unknown property " + name);
				break;
			}
		}
	}

	public void hadEvent(final Event ev) {
		log.debug("Posting message");
		bus.publish(ev);
		log.debug("Message done");
	}

	public boolean isa(final String ancestor) {
		return proxy.definition().isa(ancestor);
	}

	public boolean isa(final TangenceClass ancestor) {
		return proxy.definition().isa(ancestor);
	}

	public void eventFactory(final String name, final Object ... args) {
		final TangenceEvent def = proxy.definition().getEvent(name);
		Event ev = null;
		switch(name) {
[% FOREACH ev IN events -%]
		case "[% ev.name %]":
			ev = new [% ev.classname %](def[% IF ev.args.size %],[% END %]
[%  FOREACH arg IN ev.args -%]
				([% arg.type %]) args[[% loop.index %]][% loop.last ? '' : ',' %]
[%  END -%]
			);
			break;
[% END -%]
		default:
			log.error("Invalid name: " + name);
			break;
		}
		hadEvent(ev);
	}

// Property accessors
[% FOREACH prop IN properties -%]
[%  IF prop.smashed -%]
	/**
	 * [% prop.name %] accessor. This is a smashed property so the value is always
	 * available immediately, and any changes will propagate directly to the server.
	 */
	public [% prop.java_aggregate_type %] [% prop.name %]() { return this.[% prop.name %]; }
	public Future [% prop.name %](final [% prop.java_aggregate_type %] [% prop.name %]) { this.[% prop.name %] = [% prop.name %]; return new Future(); }
[%  ELSE -%]
	/**
	 * [% prop.name %] accessor. This is a regular property so we return a {@link Future}
	 * representing the value on completion.
	 */
	public Future [% prop.name %]() throws TangenceException {
		return proxy.getProperty("[% prop.name %]");
	}
	public Future [% prop.name %](final [% prop.java_aggregate_type %] [% prop.name %]) { this.[% prop.name %] = [% prop.name %]; return new Future(); }
[%  END -%]

	/**
	 * Returns a Watcher for the [% prop.name %] property.
	 */
	public void [% prop.name %]_watch(final PropertyWatcher pw, boolean wantInitial) throws TangenceException {
		if(!watchers.containsKey("[% prop.name %]")) {
			watchers.put("[% prop.name %]", new ArrayList<PropertyWatcher>());
[%  UNLESS prop.smashed -%]
			proxy.watch("[% prop.name %]", wantInitial);
[%  END -%]
		}
		final List<PropertyWatcher> list = (List<PropertyWatcher>) watchers.get("[% prop.name %]");
		list.add(pw);
	}
[% END -%]

[% FOREACH thing IN ['ScalarUpdate','HashUpdateSet','HashUpdateAdd','HashUpdateRemove','QueueUpdateSet','QueueUpdatePush','QueueUpdateShift'] -%]
	@Handler
	public void dispatchProxyUpdate(final TangenceObjectProxy.[% thing %] ev) {
		final String name = ev.property().name();
		if(!watchers.containsKey(name)) {
			log.debug(String.format("No watchers for %s, giving up", name));
			return;
		}
		for(final PropertyWatcher w : watchers.get(name)) {
			w.dispatch(ev);
		}
	}
[% END -%]

// Methods
[% FOREACH method IN methods -%]
	/**
	 */
	public Future [% method.name %](
[%  FOREACH arg IN method.args -%]
		final [% arg.type %] [% arg.name %][% loop.last ? '' : ',' %]
[%  END -%]
	) throws TangenceException {
		return proxy.call("[% method.name %]"[% IF method.args.size %],[% END %]
[%  FOREACH arg IN method.args -%]
			[% arg.name %][% loop.last ? '' : ',' %]
[%  END -%]
		);
	}
[% END -%]
}
EOF

use Tangence::Compiler::Parser;
use Tangence::Constants;

my $parser = Tangence::Compiler::Parser->new;
my ($file, $path, $pkg) = @ARGV;
die 'no path' unless $path;
my $rslt = $parser->from_file($file);

my %typemap = (
	str => 'String',
	bool => 'Boolean',
	int => 'Long',
	obj => 'Object',
	any => 'Object',
	'list(str)' => 'List<String>',
	'list(any)' => 'List<Object>',
	'dict(any)' => 'Map<String,Object>',
);

my $tt = Template->new;
for my $classname (sort keys %$rslt) {
#	next unless $classname eq 'StatStream.Root';
	my $class = $rslt->{$classname};
	$classname =~ s{\.}{}g;
	say "Working on class $classname";
	say " * ISA " . $_->name for $class->direct_superclasses;
	my @super = map $_->name =~ s{\.}{}gr, $class->superclasses;
	say "Properties:";
	# Tangence::Meta::Class
	my @properties;
	my $props = $class->properties;
	foreach my $propname (sort keys %$props) {
		my $prop = $props->{$propname};
		say " * " . $prop->name . ' ' . join(' of ', DIMNAMES->[$prop->dimension], $prop->type->sig) .  ($prop->smashed ? ' (smashed)' : '');
		my %def = (
			type => $typemap{$prop->type->sig} // die "No type map for " . $prop->type->sig,
		);
		if($prop->dimension == DIM_SCALAR) {
			$def{java_aggregate_type} = $def{type};
		} elsif($prop->dimension == DIM_HASH) {
			$def{java_aggregate_type} = 'Map<String, ' . $def{type} . '>';
			$def{is_hash} = 1;
		} elsif($prop->dimension == DIM_QUEUE) {
			$def{java_aggregate_type} = 'List<' . $def{type} . '>';
			$def{is_list} = 1;
		} elsif($prop->dimension == DIM_ARRAY) {
			$def{java_aggregate_type} = 'List<' . $def{type} . '>';
			$def{is_list} = 1;
		} elsif($prop->dimension == DIM_OBJSET) {
			$def{java_aggregate_type} = 'Set<' . $def{type} . '>';
			$def{is_objset} = 1;
		}
		push @properties, {
			smashed => ($prop->smashed ? 1 : 0),
			name => $prop->name,
			aggregate => DIMNAMES->[$prop->dimension],
			%def,
		};
	}
	say "Events:";
	my @events;
	my $events = $class->events;
	foreach my $evname (sort keys %$events) {
		my $ev = $events->{$evname};
		say " * " . $ev->name;
		my @args;
		foreach my $arg ($ev->arguments) {
			say "   * " . $arg->type->sig . " " . $arg->name;
			push @args, {
				type => $typemap{$arg->type->sig},
				name => $arg->name,
			};
		}
		push @events, {
			classname => ucfirst($ev->name) . 'Event',
			name => $ev->name,
			args => \@args,
		};
	}
	say "Methods:";
	my @methods;
	my $methods = $class->methods;
	foreach my $methodname (sort keys %$methods) {
		my $meth = $methods->{$methodname};
		say " * " . $meth->name;
		my @args;
		foreach my $arg ($meth->arguments) {
			say "   * " . $arg->type->sig . " " . $arg->name;
			push @args, {
				type => $typemap{$arg->type->sig},
				name => $arg->name,
			};
		}
		my $ret = $meth->ret;
		say "   ->" . $ret->sig if $ret;
		push @methods, {
			name => $meth->name,
			args => \@args,
		};
	}
	# Tangence::Meta::Class
	my %spec = (
		pkg        => $pkg,
		classname  => $classname,
		super      => \@super,
		events     => \@events,
		methods    => \@methods,
		properties => \@properties,
	);
	if(1) {
		$tt->process(
			\$interface,
			\%spec,
			"$path/tan/$classname.java"
		);
		$tt->process(
			\$implementation,
			\%spec,
			"$path/gen/$classname.java"
		);
	}
}

$tt->process(
	\$factory, {
		pkg        => $pkg,
	},
	"$path/gen/Factory.java"
);
