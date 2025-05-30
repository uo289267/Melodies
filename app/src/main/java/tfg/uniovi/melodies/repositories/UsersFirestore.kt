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
import java.util.UUID

class UsersFirestore (val userUUID: UUID){
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")


    suspend fun getFolderById(folderId: String): Folder? {
        return try {
            val document = usersCollection.document(userUUID.toString())
                            .collection("folders")
                            .document(folderId).get().await()
            if (document.exists()) {
                doc2folder(document)
                //document.toObject(Folder::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle error (log, throw custom exception, etc.)
            println("Error getting song: $e")
            null
        }
    }

    suspend fun getAllFolders(): List<Folder> {
        return try {
            val result = usersCollection.document(userUUID.toString())
                .collection("folders").orderBy("creationTime",
                                                            Query.Direction.ASCENDING)
                                                            .get().await()
            result.documents.mapNotNull { doc ->
                doc?.let { doc2folder(it) }
            }
        } catch (e: Exception) {
            Log.e("FIRESTORE", "Error getting folders", e)
            emptyList()
        }
    }


    suspend fun addFolder(dto : FolderDTO) : String?{
        val data = hashMapOf(
            "name" to dto.name,
            "creationTime" to Timestamp.now(),
            "color" to dto.color)
        return try {
            val documentReference = usersCollection.document(userUUID.toString())
                .collection("folders").add(data).await()
            documentReference.id // Return the new document ID
        } catch (e: Exception) {
            // Handle error
            println("Error adding song: $e")
            null
        }
    }
    // Coroutine-based function to delete a song
    suspend fun deleteFolder(folderId: String) {
        try {
            usersCollection.document(userUUID.toString())
                .collection("folders").document(folderId).delete().await()
        } catch (e: Exception) {
            // Handle error
            println("Error deleting song: $e")
        }
    }

    suspend fun getAllSheetsFromFolder(folderId: String): List<MusicXMLSheet> {
        return try {
            val result = usersCollection.document(userUUID.toString())
                .collection("folders")
                .document(folderId) // Filter with folderId
                .collection("sheets")
                .get()
                .await()
            Log.d("FIRESTORE", folderId)

            result.documents.mapNotNull { doc ->
                doc?.let { doc2sheet(it) }
            }

        } catch (e: Exception) {
            // Handle error
            println("Error getting all songs: $e")
            emptyList()
        }
    }

    private fun docToMusicXMLSheet(data: Map<String, Any>): MusicXMLSheet {
        return MusicXMLSheet(
            data["name"].toString(),
            data["musicxml"].toString(),
            data["author"].toString(), data["id"].toString())//puede que este mal
    }

    private suspend fun getAllSheets(querySnapshot: QuerySnapshot): List<MusicXMLSheet> {
        val allSheets = mutableListOf<MusicXMLSheet>()

        for (document in querySnapshot.documents) {
            val sheetsSnapshot = document.reference.collection("sheets").get().await()
            val sheets = sheetsSnapshot.documents.mapNotNull { sheetDoc ->
                sheetDoc.data?.let { docToMusicXMLSheet(it) } // revisar doctomusicxml antes
            }
            allSheets.addAll(sheets)
        }

        return allSheets
    }

    private fun doc2folder(doc:  DocumentSnapshot): Folder {
        return Folder(doc.data!!["name"].toString(),
            Colors.valueOf(doc.data!!["color"].toString().uppercase()),
            doc.data!!["creationTime"] as Timestamp,
            doc.id)
    }

    private fun doc2sheet(doc:  DocumentSnapshot): MusicXMLSheet {
        return MusicXMLSheet(doc.data!!["name"].toString(),
            doc.data!!["musicxml"].toString(),
            doc.data!!["author"].toString(), doc.id)
    }


}