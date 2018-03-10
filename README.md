This is a practice project. I cannot provide support or accept bug reports and pull requests, sorry!

This app is gradually being refactored from a legacy codebase to current standards. It will usually
compile and run, but currently crashes frequently, due to Marshmallow permissions and other problems.

### History

Sundroid was released on Google Play on 13th April 2010. It supported Android SDK versions 4 (1.6/Donut)
and up, and was developed on an HTC T-Mobile G1.

Over the next few years, more data (twilights, planets, calendars), widgets and alarms were added.
In March 2013, the app was redesigned to the Holo theme. By this time, fewer customers were willing
to pay for apps, and I was running out of ideas, so this was the last major update.

The app was removed from Google Play in 2015 for various reasons. A huge amount of work was required
to modernise it, and with sales now close to zero I could not afford to spend the required time
developing and supporting it.

I have uploaded it to GitHub as a demonstration of my work, and for use as practice. Once I finish
refactoring and redesigning the app, I plan to publish it on Google Play.

### Tech debt

Android has evolved rapidly since 2010 and the code is now very outdated, in part because I maintained
support for SDK 4 and OpenGL 1. I also avoided support libraries and made other compromises to keep
the app small for installation in phone memory on old devices - required for alarms and widgets to
work properly. I did not refactor the app to the gradle build system or Material design.

### Recent changes

* Java 8
* Ant scripts replaced with Gradle
* Standardised project structure
* Dropped OpenGL1 and Maps v1 support
* Changed minimum SDK from 4 to 21

### Ongoing changes

* Kotlin bulk conversion
* Kotlin optimisation
* Marshmallow permissions
* Material design
* Replace screen selector with nav drawer
* Refactor dialogs as fragments
* Replace uses of deprecated APIs

### Future changes

* Refactor preferences activity
* Extract strings to resources
* Extract more styles to resources
* Tests
* Lint

### Removed features

I have removed these features to simplify the initial rewrite. They may be restored later.

* **Alarms and notifications** Alarms can't bypass the lock screen on modern devices.
* **Widgets** The code is horribly complicated.

### License

Copyright 2010-2017 David Morrissey
Licensed under GNU Affero General Public License v3.0. Portions licensed under Apache License 2.0.
