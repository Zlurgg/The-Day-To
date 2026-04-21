package uk.co.zlurgg.thedayto.core.domain.usecases.theme

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.core.domain.model.ThemeMode
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

class GetThemeModeUseCase(
    private val preferencesRepository: PreferencesRepository,
) {
    operator fun invoke(): Flow<ThemeMode> =
        preferencesRepository.observeThemeMode()
}
