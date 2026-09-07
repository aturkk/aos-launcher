# R8 / Proguard Rules for AOS Launcher (Production Optimization)

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt & Dependency Injection
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.ComponentManager
-keep class * implements dagger.hilt.internal.GeneratedComponentManager

# AppWidgetHost (L002 & L008)
-keep class androidx.appcompat.widget.** { *; }

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
