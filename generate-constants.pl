#!/usr/bin/env perl 
use strict;
use warnings;
use 5.010;

use Template;
use Template::Stash; # avoid tedious 'used only once' warning
use List::UtilsBy qw(sort_by nsort_by);
use POSIX qw(strftime);

use Tangence::Constants ();

my %constants;
{
	no strict 'refs';
	$constants{messages} = +{
		map { $_ => Tangence::Constants->$_ } grep /^MSG_/, keys %{'Tangence::Constants::'}
	};
	$constants{message_map} = +{
		map { Tangence::Constants->$_, $_ } grep /^MSG_/, keys %{'Tangence::Constants::'}
	};

	$constants{version} = +{
		map { $_ => Tangence::Constants->$_ } grep /^VERSION_/, keys %{'Tangence::Constants::'}
	};

	$constants{change} = +{
		map { $_ => Tangence::Constants->$_ } grep /^CHANGE_/, keys %{'Tangence::Constants::'}
	};

	$constants{dim} = +{
		map { $_ => Tangence::Constants->$_ } grep /^DIM_/, keys %{'Tangence::Constants::'}
	};

	$constants{iter} = +{
		map { $_ => Tangence::Constants->$_ } grep /^ITER_/, keys %{'Tangence::Constants::'}
	};

	$constants{data} = +{
		map { $_ => Tangence::Constants->$_ } grep /^DATA/, keys %{'Tangence::Constants::'}
	};
}

# Make diffs easier - sort MSG_ types by the numeric value
$Template::Stash::HASH_OPS->{ nsortv } = sub {
	my $hash = shift;
	return [ nsort_by { $hash->{$_} } keys %$hash ];
};

# And group related data types while we're at it
$Template::Stash::HASH_OPS->{ dsortv } = sub {
	my $hash = shift;
	return [ sort_by { /^([^_]+)/; sprintf '%s%02X', $1, $hash->{$_} } keys %$hash ];
};

my %javaMessageTypeMap = (
	(map {; /^MSG_(.*)$/ ? ($_ => "TangenceMessage" . ucfirst(lc($1))) : () } keys %{$constants{messages}}),
	MSG_OK => 'TangenceMessageOK',
	MSG_GETPROP => 'TangenceMessageGetProp',
	MSG_SETPROP => 'TangenceMessageSetProp',
	MSG_GETPROPELEM => 'TangenceMessageGetPropElem',
	MSG_GETROOT => 'TangenceMessageGetRoot',
	MSG_GETREGISTRY => 'TangenceMessageGetRegistry',
);
$_ =~ s{_([a-z])}{uc $1}ge for values %javaMessageTypeMap;
$constants{java_message_class} = \%javaMessageTypeMap;

my $type = shift(@ARGV) or die "need a type: cpp, java, js";
my $tt = Template->new;
my %tmpl = (
	'cpp' => \q{
/**
 * Automatically generated - edit at your own risk
 *
 * Last update was probably [% last_update %]
 */
#ifndef TANGENCE_CONSTANTS_H_
#define TANGENCE_CONSTANTS_H_

#include <string>

namespace Tangence {
	/** Defined message types - this is the first byte in a message packet */
	enum {
[% FOREACH msg IN messages.nsortv -%]
		[% msg|format("%-24.24s") %] = [% messages.item(msg) | format("0x%02x") %][% UNLESS loop.last %],[% END %]
[% END #   msg IN messages.keys -%]
	};

	/** Data types and structures */
	enum {
[% FOREACH d IN data.dsortv -%]
		[% d | format("%-24.24s") %] = [% data.item(d) | format("0x%02x") %][% UNLESS loop.last %],[% END %]
[% END #   d IN data.nsortv -%]
	};

	/** Change notifications */
	enum {
[% FOREACH ch IN change.nsortv -%]
		[% ch|format("%-24.24s") %] = [% change.item(ch) | format("0x%02x") %][% UNLESS loop.last %],[% END %]
[% END #   ch IN change.nsortv -%]
	};

	/** Iterators */
	enum {
[% FOREACH i IN iter.nsortv -%]
		[% i | format("%-24.24s") %] = [% iter.item(i) | format("0x%02x") %][% UNLESS loop.last %],[% END %]
[% END #   i IN iter.nsortv -%]
	};

	/** Version information */
	enum {
[% FOREACH v IN version.keys.sort -%]
		[% v | format("%-24.24s") %] = [% version.item(v) | format("0x%02x") %][% UNLESS loop.last %],[% END %]
[% END #   ch IN change.nsortv -%]
	};

	std::string messageType(uint8_t msg) {
		switch(msg) {
		default:
			return "Unknown";
[% FOREACH msg IN messages.nsortv -%]
		case [% msg %]: return "[% msg %]";
[% END #   msg IN messages.keys -%]
		}
	}

	/*
	void handleMessage(uint8_t msg) {
		switch(msg) {
		default:
			throw new std::runtime_error("Unknown message type");
[% FOREACH msg IN messages.nsortv -%]
		case [% msg %]: [% msg.lower %]();
[% END #   msg IN messages.keys -%]
		}
	}
	*/
};
#endif
},	java => \q{package org.tangence.java;
/**
 * Automatically generated - edit at your own risk
 *
 * Last update was probably [% last_update %]
 */

public class Constants {
	/** Version constants */
	public static final byte MAJOR_VERSION = [% version.VERSION_MAJOR %];
	public static final byte MINOR_VERSION = [% version.VERSION_MINOR %];
	public static final byte MINOR_VERSION_MIN = 2;

	public static TangenceMessage classFromType(final int type, final long length, final Registry registry) throws TangenceException {
		switch(type) {
[% FOREACH k IN java_message_class.keys.sort -%]
		case [% k %]: return new [% java_message_class.item(k) %](type, length, registry);
[% END -%]
		default: throw new TangenceException("Unknown type " + String.valueOf(type));
		}
	}

	/** Defined message types - this is the first byte in a message packet */
[% FOREACH msg IN messages.nsortv -%]
	public static final int [% msg|format("%-24.24s") %] = [% messages.item(msg) | format("0x%02x") %];
[% END #   msg IN messages.keys -%]

[% FOREACH d IN data.dsortv -%]
	public static final int [% d | format("%-24.24s") %] = [% data.item(d) | format("0x%02x") %];
[% END #   d IN data.nsortv -%]

	/** Change notifications */
[% FOREACH ch IN change.nsortv -%]
	public static final int [% ch|format("%-24.24s") %] = [% change.item(ch) | format("0x%02x") %];
[% END #   ch IN change.nsortv -%]

	/** Property dimension sizes */
[% FOREACH ch IN dim.nsortv -%]
	public static final int [% ch|format("%-24.24s") %] = [% dim.item(ch) | format("0x%02x") %];
[% END #   ch IN dim.nsortv -%]

	/** Property dimension sizes */
[% FOREACH ch IN iter.nsortv -%]
	public static final int [% ch|format("%-24.24s") %] = [% iter.item(ch) | format("0x%02x") %];
[% END #   ch IN iter.nsortv -%]

	/** Returns the string equivalent for a message ID */
	public static String messageName(final int id) {
		switch(id) {
[% FOREACH msg IN messages.nsortv -%]
		case [% msg %]: return "[% msg.lower %]";
[% END #   msg IN messages.keys -%]
		default: break;
		}
		return "unknown";
	}
};
}, js => \q{/**
 * Automatically generated - edit at your own risk
 *
 * Last update was probably [% last_update %]
 */

/**
 * @const
 * @type {number}
 */
exports.MAJOR_VERSION = 0;
/**
 * @const
 * @type {number}
 */
exports.MINOR_VERSION = 3;
/**
 * @const
 * @type {number}
 */
exports.MINOR_VERSION_MIN = 2;

/*
public static TangenceMessage classFromType(int type, long length) throws TangenceException {
	switch(type) {
[% FOREACH k IN java_message_class.keys.sort -%]
	case [% k %]: return new [% java_message_class.item(k) %](type, length);
[% END -%]
	default: throw new TangenceException("Unknown type " + String.valueOf(type));
	}
}
*/

/* Defined message types - this is the first byte in a message packet */
[% FOREACH msg IN messages.nsortv -%]
/**
 * @const
 * @type {number}
 */
exports.[% msg|format("%-24.24s") %] = [% messages.item(msg) | format("0x%02x") %];
[% END #   msg IN messages.keys -%]

[% FOREACH d IN data.dsortv -%]
/**
 * @const
 * @type {number}
 */
exports.[% d | format("%-24.24s") %] = [% data.item(d) | format("0x%02x") %];
[% END #   d IN data.nsortv -%]

/* Change notifications */
[% FOREACH ch IN change.nsortv -%]
/**
 * @const
 * @type {number}
 */
exports.[% ch | format("%-24.24s") %] = [% change.item(ch) | format("0x%02x") %];
[% END #   ch IN change.nsortv -%]

/* ... */

/**
 * @const
 * @type {Array.<?string>}
 */
exports.MESSAGE_TYPE = [
[% FOREACH idx IN [0..255] -%]
[%  IF message_map.exists(idx) -%]
	'[% message_map.item(idx) %]'[% UNLESS loop.last %],[% END %]
[%  ELSE -%]
	null[% UNLESS loop.last %],[% END %]
[%  END -%]
[% END #   msg IN messages.keys -%]
];

/** Returns the string equivalent for a message ID */
exports.messageName = function(id) {
	switch(id) {
[% FOREACH msg IN messages.nsortv -%]
	case exports.[% msg %]: return "[% msg %]";
[% END #   msg IN messages.keys -%]
	default: break;
	}
	return "unknown";
};
}
);
$tt->process($tmpl{$type}, {
	%constants,
	last_update => strftime('%Y-%m-%dT%H:%M:%S', gmtime)
}) or die $tt->error;

