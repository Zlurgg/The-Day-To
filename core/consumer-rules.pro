# Consumer ProGuard rules for core module
# These rules are automatically applied to apps that depend on this module

# Keep Result sealed class hierarchy
-keep class io.github.zlurgg.core.domain.result.Result { *; }
-keep class io.github.zlurgg.core.domain.result.Result$* { *; }

# Keep DataError enum classes
-keep class io.github.zlurgg.core.domain.error.DataError { *; }
-keep class io.github.zlurgg.core.domain.error.DataError$* { *; }
