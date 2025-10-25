# TinyAmp Neural Audio Player - ProGuard Rules

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep TensorFlow Lite
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

# Keep model classes
-keep class com.superintelligence.tinyamp.model.** { *; }

# Keep service binders
-keep class * extends android.os.Binder

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
