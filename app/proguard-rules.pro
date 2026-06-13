# ============================================================================
# DoroMusic ProGuard Rules
# ============================================================================

# ---------- 通用保留 ----------
-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,Signature
-renamesourcefileattribute SourceFile

# ---------- Room 数据库 ----------
-keep class com.doro.music.data.db.entities.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverter class * { *; }
-dontwarn androidx.room.**

# ---------- Kotlinx Serialization ----------
-keep @kotlinx.serialization.Serializable class * { *; }
-keep,includedescriptorclasses class **$$serializer { *; }
-keep class **$Companion { *; }
-keepclassmembers class * {
    *** Companion;
    *** serializer(...);
}
-keepclassmembers class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# ---------- Navigation3 ----------
-keep class com.doro.music.ui.screen.main.MainRoute { *; }
-keep class com.doro.music.ui.screen.main.Main { *; }
-keep class com.doro.music.ui.screen.main.FolderList { *; }
-keep class com.doro.music.ui.screen.main.FolderSongs { *; }
-keep class com.doro.music.ui.screen.settings.Settings { *; }
-keep class com.doro.music.ui.screen.settings.SettingsNavKey { *; }
-keep class com.doro.music.ui.screen.settings.SettingsNavKey$* { *; }
-keep class com.doro.music.ui.screen.search.Search { *; }
-keep class com.doro.music.ui.screen.other.SongList { *; }
-keep class com.doro.music.ui.screen.other.SongDetail { *; }

# ---------- Media3 (ExoPlayer) ----------
# Media3 库自带 consumer-rules，无需全局 keep
-dontwarn androidx.media3.**
-keepclassmembers class * {
    @androidx.media3.common.util.UnstableApi *;
}

# ---------- Koin (依赖注入) ----------
# Koin 库自带 consumer-rules，应用层无需全局 keep
# 仅保留应用自定义的 DI 模块（由 Room/数据模型规则覆盖）
-dontwarn org.koin.**

# ---------- Coil (图片加载) ----------
# Coil 库自带 consumer-rules，无需全局 keep
-dontwarn coil3.**

# ---------- Firebase ----------
# Firebase 库自带 consumer-rules，无需全局 keep
-dontwarn com.google.firebase.**

# ---------- Kotlin Coroutines ----------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---------- Paging ----------
# Paging 库自带 consumer-rules，无需全局 keep
-dontwarn androidx.paging.**

# ---------- DataStore ----------
# DataStore 库自带 consumer-rules，无需全局 keep
-dontwarn androidx.datastore.**

# ---------- Kermit (日志) ----------
# Kermit 库自带 consumer-rules，无需全局 keep
-dontwarn co.touchlab.kermit.**

# ---------- AndroidX 通用 ----------
# 各 AndroidX 子库自带 proguard 规则，无需全局 keep
# 仅 dontwarn 即可，具体 keep 由各库 consumer-rules 处理
-dontwarn androidx.**

# ---------- Kotlin 标准库 ----------
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**

# ---------- 应用数据模型 (序列化/反序列化) ----------
-keep class com.doro.music.data.model.** { *; }
