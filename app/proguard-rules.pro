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
-libraryjars libs/jackson-annotations-2.0.0.jar
-libraryjars libs/jackson-core-2.0.0.jar

-dontskipnonpubliclibraryclassmembers

-keepattributes *Annotation*,EnclosingMethod

-keepnames class org.codehaus.jackson.** { *; }

-dontwarn javax.xml.**
-dontwarn javax.xml.stream.events.**
-dontwarn com.fasterxml.jackson.databind.**

-keep public class SpotifySong.** {
  public void set*(***);
  public *** get*();
}