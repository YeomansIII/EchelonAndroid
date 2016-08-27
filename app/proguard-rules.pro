# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/jason/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontnote
-keep class com.spotify.sdk.android.** { *; }
### Json SERIALIZER SETTINGS
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**
-keep class io.yeomans.echelon.models.SpotifySong.** { *; }
-keep class io.yeomans.echelon.models.SpotifySong { *; }
-keepnames class io.yeomans.echelon.models.SpotifySong.** { *; }
-keepnames class io.yeomans.echelon.models.SpotifySong { *; }
-keep class io.yeomans.echelon.models.Participant.** { *; }
-keep class io.yeomans.echelon.models.Participant { *; }
-keepnames class io.yeomans.echelon.models.Participant.** { *; }
-keepnames class io.yeomans.echelon.models.Participant { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class retrofit.** { *; }
-keep class retrofit.http.** { *; }
-keep class retrofit.client.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class kaaes.spotify.webapi.android.** { *; }
-keep class com.spotify.webapi.android.** { *; }
-keep class io.github.kaaes.spotify.webapi.** { *; }
-dontwarn retrofit.**
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn rx.**
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-dontwarn retrofit2.Platform$Java8

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
