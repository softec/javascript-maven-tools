Javascript-Maven-Tools
======================

#### Maven extensions and plug-ins for Javascript ####

The JavaScript Maven Tools improved by SOFTEC sa, aims to provide Javascript developpers
with equivalent tools to those found for Java in Maven. It brings to Javascript the whole
benefits of Maven dependency and build management and to Maven the specific tools
required for Javascript development.

Throught the use of new packaging types and lifecycles, it allow building standalone pure
JavaScript librairies, part of JEE web applications, and Titanium Appcelerator&trade;
applications for Android and iOS. It promote best development practices with standardized
project organization, so that developers can be quickly productive on existing projects
and take advantage of modular developement similar to the scheme followed in Java.

Until now, there is no real consensus on how to manage a JavaScript project. We really
hope to provide tools that will help existing users of Maven to find a new way to
manage their JavaScript projet, either integrated in a larger Java web application or as
standalone well modularized libraries.

### Targeted platforms ###

Javascript-Maven-Tools has been initially build and tested on Maven2 and should work on
any Maven2 compatible platform. It is now actively developed using Maven3

Contributing to Javascript-Maven-Tools
--------------------------------------

Fork our repository on GitHub and submit your pull request.

Documentation
-------------

A [maven documentation site](http://javascript-maven.softec.lu) is available.

Credits to forked and related projects
--------------------------------------

This project is a fork of the original project hosted at
[codehaus](http://mojo.codehaus.org/javascript-maven-tools/) which seems to be
abandoned since a while, and has never reach a release version. We introduce some major
improvements, and in particular support for Jasmine test, based on the
[Jasmine Maven Plugin](https://github.com/searls/jasmine-maven-plugin), and support
of [Appcelerator&trade; Titanium](http://www.appcelerator.com/) command line builders.

License
-------

Javascript-Maven-Tools by [SOFTEC sa](http://softec.lu) is license under
the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
If you need another license, please [contact us](mailto:support@softec.lu)
with an description of your expected usage, and we will propose you an
appropriate agreement on a case by case basis.
