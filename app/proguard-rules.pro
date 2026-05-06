-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,Signature
-renamesourcefileattribute SourceFile

-keep class com.doro.music.data.db.entities.** { *; }
-keep class com.doro.music.data.model.** { *; }

-keep class * extends androidx.room.Entity { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

-keep @kotlinx.serialization.Serializable class * { *; }
-keep,includedescriptorclasses class **$$serializer { *; }
-keep class **$Companion { *; }
-keepclassmembers class * {
    *** Companion;
    *** serializer(...);
}
-keepclassmembers class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

-keep class * implements androidx.navigation3.runtime.NavKey { *; }

-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

-keepclassmembers class * implements androidx.media3.common.MediaItem$Builder { *; }

-keepclassmembers class * {
    @androidx.media3.common.util.UnstableApi *;
}

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.**
-dontwarn org.codehaus.**
