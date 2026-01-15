package uk.co.zlurgg.thedayto.auth.data.service

import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.service.DevAuthService
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result

/**
 * No-op implementation for release builds.
 */
class NoOpDevAuthService : DevAuthService {
    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<UserData, DataError.Auth> = Result.Error(DataError.Auth.FAILED)

    override fun isAvailable(): Boolean = false
}
