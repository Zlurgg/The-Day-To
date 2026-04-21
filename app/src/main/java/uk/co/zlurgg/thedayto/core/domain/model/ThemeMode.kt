package uk.co.zlurgg.thedayto.core.domain.model

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromKey(key: String): ThemeMode =
            entries.find { it.key == key } ?: SYSTEM
    }
}
