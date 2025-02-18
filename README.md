flyerkit-droid-binaries
=======================
FlyerKit is a GUI library for displaying Flipp's flyer content in a native
Android view, supporting Android 4.0 and higher. Features include:

* Familiar pan and zoom gestures for navigation
* Handling single taps, double taps and long presses (`setTapAnnotations()`,
`setFlyerViewListener()`)
* Layering / Scaling images on top of the flyer (`setBadgeAnnotations()`)
* Highlighting parts of the flyer (`setHighlightAnnotations()`)
* Programmatically animated panning and zooming

Installation
============
FlyerKit is packaged as an Android archive (AAR) file for use with Android's
gradle build system. It can be added to your build by declaring a local
Maven repository using the flatDir command.

For example, if flyerkit-X.Y.Z.aar is located in the application's libs
directory, the build.gradle file would include:

    repositories { flatDir { dirs 'libs' } }

    dependencies {
      compile 'com.flipp.flyerkit:flyerkit:X.Y.Z@aar'
    }

Usage
=====
The primary class in FlyerKit is `com.flipp.flyerkit.FlyerView`, a View
subclass that displays the flyer given by `setFlyerId()`. Panning and
zooming are automatically enabled.

See the sample application under `sample/` for a working example. The
sample requires the constants in MainActivity to be set using your merchant
identifier and access key, along with a locale and postal code.

To build the sample, import it into Android Studio, or build and deploy from
the command line using Gradle.

API Documentation
=================
To see how to use the FlyerKit API, please refer to the following documentation:
[https://api.flipp.com/flyerkit/v3.0/documentation](https://api.flipp.com/flyerkit/v3.0/documentation).
