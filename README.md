# EdgeInsets

[![](https://jitpack.io/v/vbresan/edgeinsets.svg)](https://jitpack.io/#vbresan/edgeinsets)

A small Android library that handles edge-to-edge window insets on API 36+ with a single call.

On API 36, apps that target 36 are drawn behind the status bar, the navigation bar and display cutouts, and `Window.setStatusBarColor()` no longer has any effect. `EdgeInsets.apply(activity)` restores the layout most apps expect, without insets code in every activity.

> 💙 If this library saved you time, you are more than welcome to **say thanks** on [Ko-fi](https://ko-fi.com/vbresan/?utm_source=edgeinsets&utm_medium=github-readme&utm_campaign=regular) or [Liberapay](https://liberapay.com/vbresan/?utm_source=edgeinsets&utm_medium=github-readme&utm_campaign=regular).  
A star on the repo helps too.

## What it does

- **Colored status bar area.** A strip in your theme's `colorPrimary` is drawn behind the status bar. Its height is the status bar inset reported by the system. The status bar icons are set to light or dark depending on the strip's brightness, so they stay readable.
- **Content stays clear of the system bars.** The root view is padded: top for the status bar, bottom for the navigation bar (or the docked keyboard), and the sides for a display cutout or a side navigation bar.
- **Black bar covering the camera cutout.** In landscape, a black column, full screen height, is drawn at the side of a display cutout (camera hole), so it blends in.
- **Black 3-button navigation bar.** In landscape, a 3-button navigation bar on the side gets its own black column behind it, with the system's gray scrim turned off. The gesture navigation bar is left untouched.

Below API 36 the library does nothing. Older devices keep whatever behavior your theme already gives them, and the library never enables edge-to-edge on its own.

## Requirements

- Your activity theme defines `colorPrimary` (AppCompat and Material themes do). Without it, `apply()` throws.
- The library depends on AppCompat 1.7.1 and Material Components 1.13.0, because it reads `colorPrimary` with `MaterialColors`. Both come in transitively.
- minSdk 21.

## Installation

Add JitPack to the repositories in your `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

If your project declares repositories in the root `build.gradle`, add `maven { url "https://jitpack.io" }` to `allprojects { repositories { ... } }` instead.

Then add the dependency to your app module:

```groovy
implementation 'com.github.vbresan:edgeinsets:1.0.2'
```

## Usage

Call it once from `onCreate()`, right after `setContentView()`:

```java
import biz.binarysolutions.android.edgeinsets.EdgeInsets;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    EdgeInsets.apply(this);
}
```

- Calling it before `setContentView()` throws an `IllegalStateException`, on every API level.
- Calling it twice on the same content view is harmless. The second call is ignored.
- If you call `setContentView()` again later, call `apply()` again afterwards.
- To change the language or the theme at runtime, use `recreate()`. In testing, re-inflating the layout in place and calling `apply()` again did not receive window insets until the next configuration change.

## What it assumes

- The outermost view of your layout is the first child of `android.R.id.content`. This is true for a normal `setContentView(layoutId)`. A layout whose root tag is `<merge>` is not supported.
- The library sets an insets listener on that root view. If you set your own listener there, one of them replaces the other.

## Limitations

- The strip height comes from the system's status bar inset. In an activity with a window action bar on API 36, that inset also covered the action bar on the devices tested, so the colored strip sits behind both. The library relies on this platform behavior, and it has not been verified across all themes. In a no-title activity the strip covers only the status bar.
- The strip color is always the theme's `colorPrimary`. It cannot be set from code yet.
- A floating keyboard reports no insets, so it can cover content. The system does not give apps anything to react to in that case.
- Whether 3-button navigation icons turn white depends on the system honoring the appearance flag. Check it on a real device. It may not work on emulators.

## Tested

- An app whose activities have a window action bar.
- An app with a no-title activity.
- Portrait and landscape, with a display cutout.

## Apps using the library

[Celestial Illumination](https://play.google.com/store/apps/details?id=biz.binarysolutions.celestialillumination.ggl), [Worderer](https://play.google.com/store/apps/details?id=com.worderer)
