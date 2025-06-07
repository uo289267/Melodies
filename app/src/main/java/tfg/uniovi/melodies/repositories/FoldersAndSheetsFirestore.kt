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

class FoldersAndSheetsFirestore (val userId: String){
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")


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
        }
    }

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
        }
    }

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
        }
    }


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
        }
    }

    suspend fun addMusicXMLSheet(dto : MusicXMLDTO) : String?{
        val data = hashMapOf(
            "author" to dto.author,
            "musicxml" to dto.stringSheet,
            "name" to dto.name)
        return try {
            val documentReference = usersCollection.document(userId)
                .collection("folders")
                .document(dto.folderId)
                .collection("sheets") // Asegúrate de que la colección de partituras se llama así
                .add(data)
                .await()

            documentReference.id // Devuelve el ID del nuevo documento
        } catch (e: Exception) {
            // Handle error
            println("Error adding song: $e")
            null
        }
    }
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
        }
    }

    // Coroutine-based function to delete a song
    suspend fun deleteFolder(folderId: String) {
        try {
            usersCollection.document(userId)
                .collection("folders").document(folderId).delete().await()
        } catch (e: Exception) {
            // Handle error
            println("Error deleting song: $e")
        }
    }
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
        }
    }


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
    private fun docToMusicXMLSheet(data: Map<String, Any>, folderId: String): MusicXMLSheet {
        return MusicXMLSheet(
            data["name"].toString(),
            data["musicxml"].toString(),
            data["author"].toString(),
            data["id"].toString(),
            folderId
        )
    }

    private suspend fun getAllSheets(querySnapshot: QuerySnapshot,folderId: String ): List<MusicXMLSheet> {
        val allSheets = mutableListOf<MusicXMLSheet>()

        for (document in querySnapshot.documents) {
            val sheetsSnapshot = document.reference.collection("sheets").get().await()
            val sheets = sheetsSnapshot.documents.mapNotNull { sheetDoc ->
                sheetDoc.data?.let { docToMusicXMLSheet(it, folderId) } // revisar doctomusicxml antes
            }
            allSheets.addAll(sheets)
        }

        return allSheets
    }

    private fun doc2folder(doc:  DocumentSnapshot): Folder {
        return Folder(doc.data!!["name"].toString(),
            Colors.valueOf(doc.data!!["color"].toString().uppercase()),
            doc.id)
    }

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