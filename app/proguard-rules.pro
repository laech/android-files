# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/l/dev/android-sdk/tools/proguard/proguard-android.txt
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

# Tika references optional libs
-dontwarn org.apache.tika.**

-dontwarn com.caverock.androidsvg.**

-dontwarn com.google.common.**

-keep class com.google.android.gms.**

# Defined as l_files_operations_listeners
-keep class l.files.ui.operations.NotificationProvider

# Fields and inner classes are referenced from native code
-keep class linux.* {
  *;
}

-keep class l.files.fs.Stat {
  *;
}

# Referenced from native code
-keepclassmembers class l.files.fs.Observable {
  *** isClosed();
  *** onEvent(...);
}

# Referenced from l.files.fs.Paths
-keepclassmembers class l.files.fs.FileSystem {
  *** INSTANCE;
}
