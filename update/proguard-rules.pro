# ProGuard rules for update module

# Keep serializable DTOs
-keepclassmembers class io.github.zlurgg.update.data.remote.dto.** {
    *;
}

# Keep Ktor serialization
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
