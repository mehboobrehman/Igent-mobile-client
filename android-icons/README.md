# Android launcher icon

Copy the `res` folder into your Android app module (typically `app/src/main/res`).

Then set the application icon in `AndroidManifest.xml`:

```xml
android:icon="@mipmap/ic_launcher"
```

The `mipmap-*` folders include the standard legacy icon sizes (48–192 px) and adaptive-icon foreground layers (108–432 px). Android 8.0+ uses the adaptive icon defined in `mipmap-anydpi-v26/ic_launcher.xml`; earlier versions use the legacy PNGs.
