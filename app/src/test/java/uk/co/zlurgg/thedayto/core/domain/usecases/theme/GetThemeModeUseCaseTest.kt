package uk.co.zlurgg.thedayto.core.domain.usecases.theme

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.model.ThemeMode
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository

class GetThemeModeUseCaseTest {

    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var getThemeModeUseCase: GetThemeModeUseCase

    @Before
    fun setup() {
        fakePreferencesRepository = FakePreferencesRepository()
        getThemeModeUseCase = GetThemeModeUseCase(fakePreferencesRepository)
    }

    @Test
    fun `invoke - returns flow from repository`() = runTest {
        getThemeModeUseCase().test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - emits updated value when theme changes`() = runTest {
        getThemeModeUseCase().test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())

            fakePreferencesRepository.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem())

            fakePreferencesRepository.setThemeMode(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
