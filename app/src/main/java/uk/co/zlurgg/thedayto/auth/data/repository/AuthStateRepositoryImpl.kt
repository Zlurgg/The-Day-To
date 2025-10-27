package uk.co.zlurgg.thedayto.auth.data.repository

import android.content.Context
import android.content.SharedPreferences
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository

/**
 * Implementation of AuthStateRepository using SharedPreferences.
 *
 * Stores authentication state locally:
 * - SIGNED_IN_STATE: Boolean indicating if user is currently signed in
 *
 * Following Clean Architecture:
 * - Implements domain layer interface
 * - Uses Android SharedPreferences for persistence
 * - Injected via Koin with application context
 *
 * @param context Application context for SharedPreferences access
 */
class AuthStateRepositoryImpl(context: Context) : AuthStateRepository {

    private val pref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    private val editor = pref.edit()

    override fun setSignedInState(isSignedIn: Boolean) {
        editor.putBoolean(SIGNED_IN_STATE, isSignedIn)
        editor.commit()
    }

    override fun getSignedInState(): Boolean {
        return pref.getBoolean(SIGNED_IN_STATE, false)
    }

    companion object {
        private const val PREFERENCE_NAME = "the_day_to_preferences"
        private const val SIGNED_IN_STATE = "signed_in_state"
    }
}
