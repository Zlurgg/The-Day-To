package uk.co.zlurgg.thedayto.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromKey - system key - returns SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("system"))
    }

    @Test
    fun `fromKey - light key - returns LIGHT`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromKey("light"))
    }

    @Test
    fun `fromKey - dark key - returns DARK`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromKey("dark"))
    }

    @Test
    fun `fromKey - unknown key - defaults to SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("unknown"))
    }

    @Test
    fun `fromKey - empty key - defaults to SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey(""))
    }
}
