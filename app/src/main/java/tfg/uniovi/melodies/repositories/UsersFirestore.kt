package tfg.uniovi.melodies.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import tfg.uniovi.melodies.entities.HistoryEntry
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.repositories.config.FirestoreConfig

private const val USER_REPOSITORY = "UserRepository"

/**
 * Repository for managing user-related data in Firestore.
 *
 * Handles the creation and initialization of user documents,
 * checking for existence, and setting up default folders and sheets.
 */
class UsersFirestore (private val db: FirebaseFirestore = Firebase.firestore) {
    private val usersCollectionName = FirestoreConfig.getUsersCollectionName()
    /**
     * Checks if a nickname is already taken in Firestore.
     *
     * @param nickname The nickname to check for uniqueness.
     * @return `true` if the nickname exists, `false` otherwise.
     */
    suspend fun nicknameExists(nickname: String): Boolean {
        return try {
            val querySnapshot =db.collection(usersCollectionName)
                .whereEqualTo("nickname", nickname)
                .limit(1)
                .get()
                .await()

            !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e(USER_REPOSITORY, "Error checking nickname existence", e)
            false
        }
    }
    /**
     * Updates the nickname of a user in Firestore.
     *
     * @param userId The ID of the user document.
     * @param newNickname The new nickname to set.
     * @return true if the update was successful, false otherwise.
     * @throws DBException if nickname update fails
     */
    suspend fun updateUserNickname(userId: String, newNickname: String) {
        try {
            val userRef = db.collection(usersCollectionName) .document(userId)

            userRef.update("nickname", newNickname).await()
            Log.d(USER_REPOSITORY, "Nickname updated for user $userId -> $newNickname")
        } catch (e: Exception) {
            Log.e(USER_REPOSITORY, "Error updating nickname", e)
            throw DBException("Error while updating $userId updating: ${e.message}")
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
            val querySnapshot = db.collection(usersCollectionName)
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
            Log.e(USER_REPOSITORY, "Error retrieving userId from nickname", e)
            null
        }
    }
    /**
     * Retrieves the nickname associated with a given user ID in Firestore.
     *
     * @param userId The user ID to search for.
     * @return The nickname if found, or `null` if the user doesn't exist or an error occurs.
     */
    suspend fun getNicknameFromUserId(userId: String): String? {
        return try {
            val documentSnapshot = db.collection(usersCollectionName)
                .document(userId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                documentSnapshot.getString("nickname")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(USER_REPOSITORY, "Error retrieving nickname from userId", e)
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
     * @throws DBException when setup fails
     */

    suspend fun setupUserDataIfNeeded(nickname: String, context: Context): String? {
        val storedUserId = PreferenceManager.getUserId(context)
        if (storedUserId != null) {
            if (storedUserId.isNotEmpty()) {
                Log.d(USER_REPOSITORY, "User already exists with ID $storedUserId")
                return null //the user has already been created
            }
        }
        if(nicknameExists(nickname)){
            return null
        }

        return try {
            val userRef = db.collection(usersCollectionName).document() // ID automático
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
            Log.d(USER_REPOSITORY, "Sheets copied to user $userId")
            userId
        } catch (e: Exception) {
            Log.e(USER_REPOSITORY, "Error configuring user", e)
            throw DBException("Unable to create user $nickname")
        }
    }
    suspend fun deleteUserAndAllData(nickname: String) {
        // Buscar el documento del usuario cuyo campo "nickname" coincida
        val userQuery = db.collection(usersCollectionName)
            .whereEqualTo("nickname", nickname)
            .get()
            .await()

        if (userQuery.isEmpty) {
            println("⚠️ No se encontró ningún usuario con nickname '$nickname'")
            return
        }

        // Suponemos que el nickname es único → tomamos el primero
        val userDoc = userQuery.documents.first()
        val userRef = userDoc.reference

        // Eliminar subcolección "folders" y sus "sheets"
        val folders = userRef.collection("folders").get().await()
        for (folder in folders.documents) {
            val sheets = folder.reference.collection("sheets").get().await()
            for (sheet in sheets.documents) {
                sheet.reference.delete().await()
            }
            folder.reference.delete().await()
        }

        // Finalmente eliminar el documento del usuario
        userRef.delete().await()

        println("✅ Usuario '$nickname' y todos sus datos eliminados correctamente.")
    }



    /**
     * Retrieves the 5 most recent HistoryEntries for the current user,
     * ordered by creation time (newest first).
     *
     * @param userId whose history is retrieved
     * @return A list of up to 5 [HistoryEntry].
     * @throws DBException if retrieval fails.
     */
    suspend fun getAllHistoryEntries(userId: String): List<HistoryEntry> {
        return try {
            val result = db.collection(usersCollectionName).document(userId)
                .collection("historyEntries")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val name = doc.getString("nameOfSheet")
                val time = doc.getString("formattedTime")
                if (name != null && time != null) {
                    HistoryEntry(name, time)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw DBException("History entries could not be retrieved: ${e.message}")
        }
    }


}