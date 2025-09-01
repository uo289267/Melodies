package tfg.uniovi.melodies.preferences

import android.content.Context
/**
 * Utility object for managing app preferences related to the user.
 *
 * Handles saving, retrieving, and clearing the current user's ID
 * in [SharedPreferences].
 */
object PreferenceManager {
    private const val PREFS_NAME = "my_app_prefs"
    private const val KEY_USER_ID = "user_id"
    /**
     * Saves the given user ID in SharedPreferences.
     *
     * @param context The application context.
     * @param userId The user ID to save.
     */
    fun saveUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }
    /**
     * Retrieves the saved user ID from SharedPreferences.
     *
     * @param context The application context.
     * @return The saved user ID, or `null` if none is found.
     */
    fun getUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null)
    }
    /**
     * Clears the saved user ID from SharedPreferences.
     *
     * @param context The application context.
     */
    fun clearUserId(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}
