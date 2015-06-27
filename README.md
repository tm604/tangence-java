# Tangence Java library

This is a rough draft of a Java implementation for the [Tangence][http://tangence.org] protocol.

## Example

Start with a .tan file, and generate the Java classes:

    src/main/perl/build-super.pl example.tan generated-output/ target.java.package.name

Then inherit from those generated classes as required. 

## Developer notes

To apply any new constants etc. from the Perl implementation, run:

     generate-constants.pl java

