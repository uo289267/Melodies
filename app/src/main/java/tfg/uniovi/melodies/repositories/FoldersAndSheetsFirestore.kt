package tfg.uniovi.melodies.repositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.FolderDTO
import tfg.uniovi.melodies.fragments.viewmodels.MusicXMLDTO

private const val FIRESTORE = "FIRESTORE"

/**
 * Repository for managing folders and MusicXML sheets in Firestore.
 *
 * @property userId The ID of the authenticated Firebase user.
 */

    class FoldersAndSheetsFirestore(
        private val userId: String
        //internal val usersCollection: CollectionReference = Firebase.firestore.collection("users")
    ){
    private val db = Firebase.firestore

    /**
     * Retrieves a folder given its ID.
     *
     * @param folderId The ID of the folder.
     * @return The [Folder] if it exists
     * @throws DBException if folder is not found
     */
    suspend fun getFolderById(folderId: String): Folder? {
        return try {
            val document = db.collection("users").document(userId)
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
            val document = db.collection("users").document(userId)
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
            val result = db.collection("users").document(userId)
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
            val documentReference = db.collection("users").document(userId)
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
            val documentReference = db.collection("users").document(userId)
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
            val documentReference = db.collection("users").document(userId)
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
            val documentReference = db.collection("users").document(userId)
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
            db.collection("users").document(userId)
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
            db.collection("users").document(userId)
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
            val result = db.collection("users").document(userId)
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
            val result = db.collection("users").document(userId)
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
     * Retrieves all sheets from a query result of folders.
     *
     * Iterates through all documents (folders) in the query and
     * fetches the sheets stored in each one.
     *
     * @param querySnapshot The snapshot containing folder documents.
     * @param folderId The ID of the folder associated with the sheets.
     * @return A list of [MusicXMLSheet].
     */
    private suspend fun getAllSheets(querySnapshot: QuerySnapshot,folderId: String ): List<MusicXMLSheet> {
        val allSheets = mutableListOf<MusicXMLSheet>()

        for (document in querySnapshot.documents) {
            val sheetsSnapshot = document.reference.collection("sheets").get().await()
            val sheets = sheetsSnapshot.documents.mapNotNull { sheetDoc ->
                sheetDoc.data?.let { docToMusicXMLSheet(it, folderId) }
            }
            allSheets.addAll(sheets)
        }

        return allSheets
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




}