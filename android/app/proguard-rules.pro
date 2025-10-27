# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep JavaScript interface
-keepclassmembers class com.superintelligence.tinyamp.bridge.WebAppInterface {
    public *;
}

# Keep WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Media3
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**
