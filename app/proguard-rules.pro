# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep generic metadata used by Retrofit, Kotlin serialization and Hilt-generated code.
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, AnnotationDefault

# Retrofit service interfaces are discovered reflectively.
-keep interface com.abdulin.nutritionapp.data.remote.** { *; }

# Keep serializable DTO metadata for Kotlin serialization.
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class **$$serializer { *; }

# Keep all DTO classes (used by kotlinx.serialization via Retrofit)
-keep class com.abdulin.nutritionapp.data.dto.** { *; }

# Keep API response wrapper
-keep class com.abdulin.nutritionapp.data.remote.**ResponseDto { *; }
-keep class com.abdulin.nutritionapp.data.remote.**Response { *; }

# WorkManager workers are instantiated by name.
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep Hilt entry points
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room entities
-keep class com.abdulin.nutritionapp.data.local.entity.** { *; }

# Keep Navigation arguments
-keepnames class * implements android.os.Parcelable
-keepnames class * implements java.io.Serializable
