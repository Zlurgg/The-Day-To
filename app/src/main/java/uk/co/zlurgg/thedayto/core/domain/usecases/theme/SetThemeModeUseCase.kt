package uk.co.zlurgg.thedayto.core.domain.usecases.theme

import uk.co.zlurgg.thedayto.core.domain.model.ThemeMode
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

class SetThemeModeUseCase(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) {
        preferencesRepository.setThemeMode(mode)
    }
}
