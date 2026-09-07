# Mantener clases de Google Play Services para evitar problemas de handshake
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Específicamente para el error de 'calling package'
-keepattributes Signature,InnerClasses,EnclosingMethod
-keep public class com.google.android.gms.common.internal.ReflectedParcelable

# --- Moshi ---
-keep class com.squareup.moshi.** { *; }
-keepnames class * { @com.squareup.moshi.Json *; }

# --- Retrofit ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep  class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# --- OkHttp ---
-keepattributes *Annotation*
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**