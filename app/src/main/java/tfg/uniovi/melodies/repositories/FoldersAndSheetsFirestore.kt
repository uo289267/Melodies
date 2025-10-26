package tfg.uniovi.melodies.repositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import tfg.uniovi.melodies.model.Colors
import tfg.uniovi.melodies.model.Folder
import tfg.uniovi.melodies.model.HistoryEntry
import tfg.uniovi.melodies.model.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.FolderDTO
import tfg.uniovi.melodies.fragments.viewmodels.MusicXMLDTO
import tfg.uniovi.melodies.repositories.config.FirestoreConfig

private const val FIRESTORE = "FIRESTORE"

/**
 * Repository for managing folders and MusicXML sheets in Firestore.
 *
 * @property userId The ID of the authenticated Firebase user.
 */
class FoldersAndSheetsFirestore (private val userId: String,
                                 private val usersCollection: CollectionReference? = null ){

    private val usersColl: CollectionReference = usersCollection ?: Firebase.firestore.collection(
        FirestoreConfig.getUsersCollectionName())


    /**
     * Retrieves a folder given its ID.
     *
     * @param folderId The ID of the folder.
     * @return The [Folder] if it exists
     * @throws DBException if folder is not found
     */
    suspend fun getFolderById(folderId: String): Folder? {
        return try {
            val document = usersColl.document(userId)
                            .collection("folders")
                            .document(folderId).get().await()
            if (document.exists()) {
                doc2folder(document)
            } else {
                null
            }
        } catch (e: Exception) {
            throw DBException("Folder with id $folderId could not be found: $e.message")
        }
    }

    /**
     * Retrieves a MusicXML sheet given its ID within a given folder.
     *
     * @param sheetId The ID of the sheet.
     * @param folderId The ID of the folder containing the sheet.
     * @return The [MusicXMLSheet] if it exists, or `null` otherwise.
     */
    suspend fun getSheetById(sheetId: String, folderId: String): MusicXMLSheet?{
        return try{
            val document = usersColl.document(userId)
                            .collection("folders")
                            .document(folderId)
                            .collection("sheets")
                            .document(sheetId).get().await()
            if (document.exists()){
                doc2sheet(document, folderId)
            }else{
                null
            }
        } catch (e: Exception) {
            throw DBException("Sheet with id $sheetId could not be found: $e.message")
        }
    }
    /**
     * Retrieves all folders for the current user, ordered by creation time.
     *
     * @return A list of [Folder], or an empty list if an error occurs.
     */
    suspend fun getAllFolders(): List<Folder> {
        return try {
            val result = usersColl.document(userId)
                .collection("folders").orderBy("creationTime",
                                                            Query.Direction.ASCENDING)
                                                            .get().await()
            result.documents.mapNotNull { doc ->
                doc?.let { doc2folder(it) }
            }
        } catch (e: Exception) {
            throw DBException("Folders could not be retrieved: $e.message")
        }
    }

    /**
     * Adds a new folder to Firestore.
     *
     * @param dto A [FolderDTO] object containing the folder data.
     * @return The generated folder ID, or `null` if an error occurs.
     * @throws DBException if the new folder was not added
     */
    suspend fun addFolder(dto : FolderDTO) : String?{
        val data = hashMapOf(
            "name" to dto.name,
            "creationTime" to Timestamp.now(),
            "color" to dto.color.name)
        return try {
            val documentReference = usersColl.document(userId)
                .collection("folders").add(data).await()
            documentReference.id // Return the new document ID
        } catch (e: Exception) {
            throw DBException("${dto.name} could not be added: $e.message")
        }
    }
    /**
     * Adds a new MusicXML sheet inside a folder.
     *
     * @param dto A [MusicXMLDTO] object containing the sheet data.
     * @return The generated sheet ID, or `null` if an error occurs.
     * @throws DBException if the new sheet was not added
     */
    suspend fun addMusicXMLSheet(dto : MusicXMLDTO) : String?{
        val data = hashMapOf(
            "author" to dto.author,
            "musicxml" to dto.stringSheet,
            "name" to dto.name)
        return try {
            val documentReference = usersColl.document(userId)
                .collection("folders")
                .document(dto.folderId)
                .collection("sheets")
                .add(data)
                .await()

            documentReference.id
        } catch (e: Exception) {
            throw DBException("${dto.name} could not be added: $e.message")
        }
    }
    /**
     * Updates the name of a folder in the Firestore database.
     *
     * @param folderId The unique identifier of the folder whose name will be updated.
     * @param newName The new name to assign to the folder.
     *
     * @throws DBException If the new name update fails
     */
    suspend fun setNewFolderName(folderId: String, newName: String){
        try{
            val documentReference = usersColl.document(userId)
                .collection("folders")
                .document(folderId)
            documentReference
                .update("name", newName)
                .await()
        } catch (e: Exception) {
            throw DBException("$folderId could not be renamed to $newName: $e.message")
        }
    }
    /**
     * Updates the name of an existing MusicXML sheet.
     *
     * @param sheetId The ID of the sheet to rename.
     * @param folderId The ID of the folder containing the sheet.
     * @param newName The new name to set for the sheet.
     * @throws DBException if new name update fails
     */
    suspend fun setNewSheetName(sheetId: String, folderId: String, newName: String) {
       try{
            val documentReference = usersColl.document(userId)
                .collection("folders")
                .document(folderId)
                .collection("sheets")
                .document(sheetId)
            documentReference
                .update("name", newName)
                .await()
        } catch (e: Exception) {
           throw DBException("$sheetId could not be renamed to $newName: $e.message")
        }
    }
    /**
     * Deletes a folder given its ID.
     *
     * @param folderId The ID of the folder to delete.
     * @throws DBException if folder deletion fails
     */
    suspend fun deleteFolder(folderId: String) {
        try {
            usersColl.document(userId)
                .collection("folders").document(folderId).delete().await()
        } catch (e: Exception) {
            throw DBException("$folderId could not be deleted: $e.message")
        }
    }
    /**
     * Deletes a sheet from a folder.
     *
     * @param sheetId The ID of the sheet to delete.
     * @param folderId The ID of the folder containing the sheet.
     * @throws DBException if sheet deletion fails
     */
    suspend fun deleteSheet(sheetId: String, folderId: String) {
        try {
            usersColl.document(userId)
                .collection("folders")
                .document(folderId)
                .collection("sheets")
                .document(sheetId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw DBException("$sheetId could not be deleted: $e.message")
        }
    }

    /**
     * Retrieves all sheets from a given folder.
     *
     * @param folderId The ID of the folder.
     * @return A list of [MusicXMLSheet], or an empty list if an error occurs.
     * @throws DBException if fails to retrieve sheets from folders
     */
    suspend fun getAllSheetsFromFolder(folderId: String): List<MusicXMLSheet> {
        return try {
            val result = usersColl.document(userId)
                .collection("folders")
                .document(folderId) // Filter with folderId
                .collection("sheets")
                .get()
                .await()
            Log.d(FIRESTORE, "Getting all sheets from $folderId folder")

            result.documents.mapNotNull { doc ->
                doc?.let { doc2sheet(it, folderId) }
            }

        } catch (e: Exception) {
            throw DBException("$folderId sheets could not be retrieved: $e.message")
        }
    }

    /**
     * Checks whether a folder name is already in use by the current user in Firestore.
     * - Returns `true` if at least one folder with the given name exists.
     * - Returns `false` if no folder with the given name exists.
     * - Throws [DBException] if an unexpected error occurs (e.g., network failure without cache,
     *   or insufficient Firestore permissions).
     *
     * @param folderName The folder name to check for duplicates.
     * @return `true` if the folder name is already in use, `false` if it is available.
     * @throws DBException If the Firestore query fails unexpectedly.
     */
    suspend fun isFolderNameInUse(folderName: String): Boolean? {
        return try{
            val result = usersColl.document(userId)
                .collection("folders")
                .whereEqualTo("name",folderName)
                .get()
                .await()
            !result.isEmpty
        } catch (e : Exception){
            Log.d(FIRESTORE,"Error while finding duplicates of $folderName name , ${e.message}")
            throw DBException("Unexpected exception while looking for duplicates of $folderName")
        }
    }
    /**
     * Checks whether a sheet (music score) with the given name already exists
     * within a specific folder in the current user's Firestore database.
     *
     * This function queries the path:
     * `users/{userId}/folders/{folderId}/sheets`
     * looking for documents whose `name` field matches the provided sheet name.
     *
     * @param sheetName The name of the sheet to check for duplicates.
     * @param folderId The ID of the folder in which to check for existing sheets.
     * @return `true` if a sheet with the same name already exists,
     *         `false` if no sheet with that name was found,
     *         or `null` if an unexpected error occurs before throwing an exception.
     *
     * @throws DBException If an unexpected error occurs during the Firestore query.
     *
     * @see getSheetById for retrieving a specific sheet by its ID and folder.
     */
    suspend fun isSheetNameInUse(sheetName: String, folderId: String): Boolean? {
        return try {
            val result = usersColl.document(userId)
                .collection("folders")
                .document(folderId)
                .collection("sheets")
                .whereEqualTo("name", sheetName)
                .get()
                .await()
            result.documents.forEach { Log.d("DBG", it.data.toString()) }
            !result.isEmpty
        } catch (e: Exception) {
            Log.d(FIRESTORE, "Error while finding duplicates of $sheetName in folder $folderId, ${e.message}")
            throw DBException("Unexpected exception while looking for duplicates of $sheetName in folder $folderId")
        }
    }

    /**
     * Converts a raw Firestore document data map into a [MusicXMLSheet] instance.
     *
     * @param data The raw Firestore document data as a map of field names to values.
     * @param folderId The ID of the parent folder that contains this sheet.
     * @return A [MusicXMLSheet] object populated with the provided data.
     *
     * @throws NullPointerException If any expected field is missing or null in the data map.
     */
    private fun docToMusicXMLSheet(data: Map<String, Any>, folderId: String): MusicXMLSheet {
        return MusicXMLSheet(
            data["name"].toString(),
            data["musicxml"].toString(),
            data["author"].toString(),
            data["id"].toString(),
            folderId
        )
    }
    /**
     * Converts a Firestore document into a [Folder].
     *
     * @param doc The Firestore document snapshot.
     * @return A [Folder] object with the document data.
     */
    private fun doc2folder(doc:  DocumentSnapshot): Folder {
        return Folder(doc.data!!["name"].toString(),
            Colors.valueOf(doc.data!!["color"].toString().uppercase()),
            doc.id)
    }
    /**
     * Converts a Firestore document into a [MusicXMLSheet].
     *
     * @param doc The Firestore document snapshot.
     * @param folderId The ID of the folder containing the sheet.
     * @return A [MusicXMLSheet] object with the document data.
     */
    private fun doc2sheet(doc:  DocumentSnapshot, folderId: String): MusicXMLSheet {
        return MusicXMLSheet(
            doc.data!!["name"].toString(),
            doc.data!!["musicxml"].toString(),
            doc.data!!["author"].toString(),
            doc.id,
            folderId
        )
    }

    /**
     * Retrieves the color of a folder given its ID.
     *
     * @param folderId The ID of the folder whose color is to be retrieved.
     * @return The [Colors] value representing the folder's color, or null if not found.
     * @throws DBException if the folder could not be retrieved or has no color field.
     */
    suspend fun getFolderColor(folderId: String): Colors? {
        return try {
            val document = usersColl.document(userId)
                .collection("folders")
                .document(folderId)
                .get()
                .await()

            if (document.exists()) {
                val colorName = document.getString("color")
                colorName?.let { Colors.valueOf(it.uppercase()) }
            } else {
                null
            }
        } catch (e: Exception) {
            throw DBException("Could not retrieve color for folder $folderId: ${e.message}")
        }
    }

    /**
     * Saves a new HistoryEntry for the current user.
     *
     * Each entry is stored under:
     * users/{userId}/historyEntries/{autoGeneratedId}
     *
     * @param historyEntry The [HistoryEntry] to save.
     * @throws DBException if the entry could not be saved.
     */
    suspend fun saveNewHistoryEntry(historyEntry: HistoryEntry) {
        try {
            val data = hashMapOf(
                "nameOfSheet" to historyEntry.nameOfSheet,
                "formattedTime" to historyEntry.formattedTime,
                "createdAt" to Timestamp.now()
            )

            usersColl.document(userId)
                .collection("historyEntries")
                .add(data)
                .await()

            Log.d(FIRESTORE, "New history entry saved for user $userId: ${historyEntry.nameOfSheet}")

        } catch (e: Exception) {
            throw DBException("History entry could not be saved: ${e.message}")
        }
    }





}