# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   https://developer.android.com/guide/developing/tools/proguard.html

# Minimize SDK footprint by aggressively removing unused code and resources
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
#-optimizationpasses 5
#-allowaccessmodification
#
## Room Database
#-keep class androidx.room.** { *; }
#-keep class * extends androidx.room.RoomDatabase
#-keep @androidx.room.Entity class *
#-keep @androidx.room.Dao interface *
#-dontwarn androidx.room.**
#
## Keep Retrofit/OkHttp specific classes
#-keep class retrofit2.** { *; }
#-keepattributes Signature, InnerClasses, EnclosingMethod
#-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
#-keepclassmembers,allowshrinking,allowobfuscation interface * {
#    @retrofit2.http.* <methods>;
#}
#-dontwarn retrofit2.**
#-dontwarn okhttp3.**
#-dontwarn okio.**
#-dontwarn javax.annotation.**
#
## Gson specific classes
#-keep class com.google.gson.** { *; }
#-keep class * implements com.google.gson.TypeAdapter
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer
#-keepclassmembers,allowobfuscation class * {
#  @com.google.gson.annotations.SerializedName <fields>;
#}
#
## Compose specific rules
#-keep class androidx.compose.** { *; }
#
## Koin
#-keep class org.koin.** { *; }
#-keep public class * extends org.koin.core.module.Module { *; }
#
## Keep the Tolgee SDK public API
#-keep public class cz.fit.cvut.sdk.TolgeeSdk { *; }
#-keep public class cz.fit.cvut.sdk.TolgeeSdkApi { *; }
#-keep public class cz.fit.cvut.sdk.utils.TolgeeSdkMode { *; }
#-keep public class cz.fit.cvut.sdk.TolgeeSdkProviderKt { *; }
#
## Keep any classes referenced from XML files
#-keep public class * extends android.view.View {
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
#
## General Android rules
#-keepclassmembers class * implements android.os.Parcelable {
#    public static final ** CREATOR;
#}
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile