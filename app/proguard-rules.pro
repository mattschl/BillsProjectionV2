# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @kotlinx.parcelize.Parcelize class * { *; }

# Google API Client & Drive
# Instead of keeping the whole package, keep models and fields with @Key
-keep class com.google.api.services.drive.model.** { *; }
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}
# Keep classes used for initialization
-keep class com.google.api.client.json.gson.GsonFactory
-keep class com.google.api.client.json.JsonFactory
-keep class com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
-keep class com.google.api.client.http.HttpTransport
-keep class com.google.api.client.http.javanet.NetHttpTransport

# Google Identity Services
# Keep specific classes used in the app to avoid broad keep rules
-keep class com.google.android.libraries.identity.googleid.GetGoogleIdOption
-keep class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

# Preserve line numbers for better deobfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile