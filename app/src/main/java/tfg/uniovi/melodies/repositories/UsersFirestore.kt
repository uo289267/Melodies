package tfg.uniovi.melodies.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import tfg.uniovi.melodies.preferences.PreferenceManager
/**
 * Repository for managing user-related data in Firestore.
 *
 * Handles the creation and initialization of user documents,
 * checking for existence, and setting up default folders and sheets.
 */
class UsersFirestore {
    private val db = Firebase.firestore
    /**
     * Checks if a user already exists in Firestore.
     *
     * @param userId The ID of the user to check.
     * @return `true` if the user exists, `false` otherwise.
     */
    suspend fun userExists(userId: String): Boolean {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking user existence", e)
            false
        }
    }
    /**
     * Checks if a nickname is already taken in Firestore.
     *
     * @param nickname The nickname to check for uniqueness.
     * @return `true` if the nickname exists, `false` otherwise.
     */
    suspend fun nicknameExists(nickname: String): Boolean {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("nickname", nickname)
                .limit(1)
                .get()
                .await()

            !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking nickname existence", e)
            false
        }
    }
    /**
     * Retrieves the user ID associated with a given nickname in Firestore.
     *
     * @param nickname The nickname to search for.
     * @return The user ID if found, or `null` if no user with that nickname exists or if an error occurs.
     */
    suspend fun getUserIdFromNickname(nickname: String): String? {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("nickname", nickname)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error retrieving userId from nickname", e)
            null
        }
    }

    /**
     * Sets up user data in Firestore if the user does not already exist.
     *
     * - Creates a new user document with:
     *   - A server timestamp (`createdAt`).
     *   - The provided `nickname`.
     * - Creates a default folder named **"Basic Sheets"**.
     * - Copies default sheets from the `defaultSheets` collection into the user's folder.
     * - Saves the generated user ID in [PreferenceManager].
     *
     * @param nickname The nickname to associate with the newly created user.
     * @param context The application context used to access [PreferenceManager].
     * @return The newly created user ID, or `null` if the user already exists or if an error occurs.
     */

    suspend fun setupUserDataIfNeeded(nickname: String, context: Context): String? {
        val storedUserId = PreferenceManager.getUserId(context)
        if (storedUserId != null) {
            if (storedUserId.isNotEmpty()) {
                Log.d("UserRepository", "User already exists with ID $storedUserId")
                return null //the user has already been created
            }
        }

        return try {
            val userRef = db.collection("users").document() // ID automático
            val userId = userRef.id
            // Create user document
            val userData = mapOf("createdAt" to FieldValue.serverTimestamp(), "nickname" to nickname)
            userRef.set(userData).await()
            // Create folder "Basic Sheets"
            val folderRef = userRef.collection("folders").document()
            val folderData = mapOf(
                "name" to "Basic Sheets",
                "color" to "YELLOW",
                "creationTime" to FieldValue.serverTimestamp()
            )
            folderRef.set(folderData).await()

            // Obtain sheets for basic sheets from defaultSheets document
            val result = db.collection("defaultSheets").get().await()
            for (sheetDoc in result) {
                val sheetData = sheetDoc.data
                folderRef.collection("sheets").add(sheetData).await()
            }

            PreferenceManager.saveUserId(context, userId)
            Log.d("UserRepository", "Sheets copied to user $userId")
            userId
        } catch (e: Exception) {
            Log.e("UserRepository", "Error configuring user", e)
            null
        }
    }


}