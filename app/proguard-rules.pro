# ProGuard/R8 Rules for The Day To
# Modern Android best practices - minimal, targeted rules

# ============================================================================
# DEBUGGING - Keep line numbers for crash reports
# ============================================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================================
# KOTLIN
# ============================================================================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# ============================================================================
# ROOM DATABASE
# ============================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============================================================================
# RETROFIT / OKHTTP
# ============================================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*

# ============================================================================
# FIREBASE
# ============================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ============================================================================
# KOIN
# ============================================================================
-keep class org.koin.** { *; }
-keepclassmembers class * {
    public <init>(...);
}

# ============================================================================
# OPENCSV
# ============================================================================
-dontwarn com.opencsv.**
-keep class com.opencsv.** { *; }

# ============================================================================
# APP-SPECIFIC - Domain models used with Room
# ============================================================================
-keep class uk.co.zlurgg.thedayto.**.model.** { *; }
-keep class uk.co.zlurgg.thedayto.**.data.model.** { *; }
