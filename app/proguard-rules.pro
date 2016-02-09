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
-keep class com.spotify.sdk.android.** { *; }
### Json SERIALIZER SETTINGS
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**
-keep class io.yeomans.echelon.SpotifySong.** { *; }
-keep class io.yeomans.echelon.SpotifySong { *; }
-keepnames class io.yeomans.echelon.SpotifySong.** { *; }
-keepnames class io.yeomans.echelon.SpotifySong { *; }
-keep class io.yeomans.echelon.Participant.** { *; }
-keep class io.yeomans.echelon.Participant { *; }
-keepnames class io.yeomans.echelon.Participant.** { *; }
-keepnames class io.yeomans.echelon.Participant { *; }
-keepattributes Signature
-keep class retrofit.** { *; }
-keep class retrofit.http.** { *; }
-keep class retrofit.client.** { *; }
-dontwarn retrofit.**
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn rx.**