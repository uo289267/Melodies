package tfg.uniovi.melodies.repositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
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
/**
 * Repository for managing folders and MusicXML sheets in Firestore.
 *
 * @property userId The ID of the authenticated Firebase user.
 */
class FoldersAndSheetsFirestore (private val userId: String){
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    /**
     * Retrieves a folder given its ID.
     *
     * @param folderId The ID of the folder.
     * @return The [Folder] if it exists, or `null` otherwise.
     */
    suspend fun getFolderById(folderId: String): Folder? {
        return try {
            val document = usersCollection.document(userId)
                            .collection("folders")
                            .document(folderId).get().await()
            if (document.exists()) {
                doc2folder(document)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error getting song: $e")
            null
            // TODO lanzar custom exception?
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
            val document = usersCollection.document(userId)
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
            println("Error getting song: $e")
            null
            // TODO lanzar custom exception?
        }
    }
    /**
     * Retrieves all folders for the current user, ordered by creation time.
     *
     * @return A list of [Folder], or an empty list if an error occurs.
     */
    suspend fun getAllFolders(): List<Folder> {
        return try {
            val result = usersCollection.document(userId)
                .collection("folders").orderBy("creationTime",
                                                            Query.Direction.ASCENDING)
                                                            .get().await()
            result.documents.mapNotNull { doc ->
                doc?.let { doc2folder(it) }
            }
        } catch (e: Exception) {
            Log.e("FIRESTORE", "Error getting folders", e)
            emptyList()
            // TODO lanzar custom exception?
        }
    }

    /**
     * Adds a new folder to Firestore.
     *
     * @param dto A [FolderDTO] object containing the folder data.
     * @return The generated folder ID, or `null` if an error occurs.
     */
    suspend fun addFolder(dto : FolderDTO) : String?{
        val data = hashMapOf(
            "name" to dto.name,
            "creationTime" to Timestamp.now(),
            "color" to dto.color.name)
        return try {
            val documentReference = usersCollection.document(userId)
                .collection("folders").add(data).await()
            documentReference.id // Return the new document ID
        } catch (e: Exception) {
            // Handle error
            println("Error adding song: $e")
            null
            // TODO lanzar custom exception?
        }
    }
    /**
     * Adds a new MusicXML sheet inside a folder.
     *
     * @param dto A [MusicXMLDTO] object containing the sheet data.
     * @return The generated sheet ID, or `null` if an error occurs.
     */
    suspend fun addMusicXMLSheet(dto : MusicXMLDTO) : String?{
        val data = hashMapOf(
            "author" to dto.author,
            "musicxml" to dto.stringSheet,
            "name" to dto.name)
        return try {
            val documentReference = usersCollection.document(userId)
                .collection("folders")
                .document(dto.folderId)
                .collection("sheets")
                .add(data)
                .await()

            documentReference.id
        } catch (e: Exception) {
            // Handle error
            println("Error adding song: $e")
            null
            // TODO lanzar custom exception?
        }
    }
    /**
     * Updates the name of an existing MusicXML sheet.
     *
     * @param sheetId The ID of the sheet to rename.
     * @param folderId The ID of the folder containing the sheet.
     * @param newName The new name to set for the sheet.
     */
    suspend fun setNewSheetName(sheetId: String, folderId: String, newName: String) {
       try{
            val documentReference = usersCollection.document(userId)
                .collection("folders")
                .document(folderId)
                .collection("sheets")
                .document(sheetId)
            documentReference
                .update("name", newName)
                .await()
        } catch (e: Exception) {
            // Handle error
            println("Error renaming song $sheetId: $e")
           // TODO lanzar custom exception?
        }
    }
    /**
     * Deletes a folder given its ID.
     *
     * @param folderId The ID of the folder to delete.
     */
    suspend fun deleteFolder(folderId: String) {
        try {
            usersCollection.document(userId)
                .collection("folders").document(folderId).delete().await()
        } catch (e: Exception) {
            // Handle error
            println("Error deleting song: $e")
            // TODO lanzar custom exception?
        }
    }
    /**
     * Deletes a sheet from a folder.
     *
     * @param sheetId The ID of the sheet to delete.
     * @param folderId The ID of the folder containing the sheet.
     */
    suspend fun deleteSheet(sheetId: String, folderId: String) {
        try {
            usersCollection.document(userId)
                .collection("folders")
                .document(folderId)
                .collection("sheets")
                .document(sheetId)
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle error
            println("Error deleting song: $e")
            // TODO lanzar custom exception?
        }
    }

    /**
     * Retrieves all sheets from a given folder.
     *
     * @param folderId The ID of the folder.
     * @return A list of [MusicXMLSheet], or an empty list if an error occurs.
     */
    suspend fun getAllSheetsFromFolder(folderId: String): List<MusicXMLSheet> {
        return try {
            val result = usersCollection.document(userId)
                .collection("folders")
                .document(folderId) // Filter with folderId
                .collection("sheets")
                .get()
                .await()
            Log.d("FIRESTORE", folderId)

            result.documents.mapNotNull { doc ->
                doc?.let { doc2sheet(it, folderId) }
            }

        } catch (e: Exception) {
            // Handle error
            println("Error getting all songs: $e")
            emptyList()
            // TODO lanzar custom exception?
        }
    }

    suspend fun isFolderNameInUse(folderName: String): Boolean? {
        return try{
            val result = usersCollection.document(userId)
                .collection("folders")
                .whereEqualTo("name",folderName)
                .get()
                .await()
            !result.isEmpty
        } catch (e : Exception){
            Log.d("FIREBASE","Error while finding duplicates of $folderName name , ${e.message}")
            false
        }
    }
    /**
     * Checks if a folder name is already in use.
     *
     * @param folderName The name of the folder to check.
     * @return `true` if the name is already in use, `false` otherwise.
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