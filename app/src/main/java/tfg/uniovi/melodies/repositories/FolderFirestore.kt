package tfg.uniovi.melodies.repositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.entities.MusicXMLSheet

class FolderFirestore { //TODO DELETE
    private val db = Firebase.firestore
    private val foldersColletion = db.collection("folders")

    suspend fun getFolderById(folderId: String): Folder? {
        return try {
            val document = foldersColletion.document(folderId).get().await()
            if (document.exists()) {
                document.toObject(Folder::class.java) // Assuming you have a Song data class
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle error (log, throw custom exception, etc.)
            println("Error getting song: $e")
            null
        }
    }
    /*
    suspend fun getAllFolders(): List<Folder> {
        return try {
            val result = foldersColletion.get().await()
            result.documents.mapNotNull { doc ->
                doc?.let { doc2folder(it) }
            }
        } catch (e: Exception) {
            Log.e("FIRESTORE", "Error getting folders", e)
            emptyList()
        }
    }*/


    suspend fun addFolder(folder:Folder) : String?{
        return try {
            val documentReference = foldersColletion.add(folder).await()
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
            foldersColletion.document(folderId).delete().await()
        } catch (e: Exception) {
            // Handle error
            println("Error deleting song: $e")
        }
    }

    suspend fun getAllSheetsFromFolder(folderId: String): List<MusicXMLSheet> {
        return try {
            val querySnapshot = foldersColletion
                .whereEqualTo("folderId", folderId) // Filtramos por folderId
                .get()
                .await()
            getAllSheets(querySnapshot)

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
            data["author"].toString(), data["id"].toString())
    }

    private suspend fun getAllSheets(querySnapshot: QuerySnapshot): List<MusicXMLSheet> {
        val allSheets = mutableListOf<MusicXMLSheet>()

        for (document in querySnapshot.documents) {
            val sheetsSnapshot = document.reference.collection("sheets").get().await()
            val sheets = sheetsSnapshot.documents.mapNotNull { sheetDoc ->
                sheetDoc.data?.let { docToMusicXMLSheet(it) }
            }
            allSheets.addAll(sheets)
        }

        return allSheets
    }
/*
    private fun doc2folder(doc:  DocumentSnapshot): Folder {
        return Folder(doc.data!!["name"].toString(),
            doc.data!!["color"].toString().toInt(),
            doc.data!!["creationTime"] as Timestamp,
            doc.id)
    }*/

}