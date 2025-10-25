# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes
-keep class com.tinyamp.player.data.models.** { *; }

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Glide classes
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
