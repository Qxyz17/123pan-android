# Keep Gson classes
-keep class com.pan123nextgen.android.api.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**