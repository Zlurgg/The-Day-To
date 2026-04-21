package uk.co.zlurgg.thedayto.core.domain.usecases.theme

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.model.ThemeMode
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository

class SetThemeModeUseCaseTest {

    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var setThemeModeUseCase: SetThemeModeUseCase

    @Before
    fun setup() {
        fakePreferencesRepository = FakePreferencesRepository()
        setThemeModeUseCase = SetThemeModeUseCase(fakePreferencesRepository)
    }

    @Test
    fun `invoke - delegates to repository`() = runTest {
        setThemeModeUseCase(ThemeMode.DARK)

        fakePreferencesRepository.observeThemeMode().test {
            assertEquals(ThemeMode.DARK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - sets light mode`() = runTest {
        setThemeModeUseCase(ThemeMode.LIGHT)

        fakePreferencesRepository.observeThemeMode().test {
            assertEquals(ThemeMode.LIGHT, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - sets system mode`() = runTest {
        // Start with a different mode
        setThemeModeUseCase(ThemeMode.DARK)
        setThemeModeUseCase(ThemeMode.SYSTEM)

        fakePreferencesRepository.observeThemeMode().test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
