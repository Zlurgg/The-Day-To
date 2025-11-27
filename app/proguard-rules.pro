# ProGuard/R8 Rules for The Day To
# Minimal rules - most libraries bundle their own consumer rules

# ============================================================================
# DEBUGGING - Keep line numbers for crash reports
# ============================================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================================
# KOTLIN SERIALIZATION (if using reflection)
# ============================================================================
-keepattributes *Annotation*

# ============================================================================
# ROOM - Keep entities (accessed via reflection)
# ============================================================================
-keep @androidx.room.Entity class * { *; }

# ============================================================================
# SUPPRESS WARNINGS - Libraries without full R8 support
# ============================================================================
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
