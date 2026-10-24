# Add project-specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# --- Kotlin Serialization ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep,allowobfuscation @interface kotlinx.serialization.Serializable
-keep,allowobfuscation @interface kotlinx.serialization.SerialName
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# --- Retrofit & OkHttp ---
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
# -suppresswarnings okhttp3.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# --- Hilt DI ---
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentComponentBuilder { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager$ApplicationComponentCreator { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @javax.inject.Inject class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# --- Jetpack Compose ---
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}
-dontwarn androidx.compose.**

# --- WorkManager ---
-keep class androidx.work.ListenableWorker { *; }
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**
