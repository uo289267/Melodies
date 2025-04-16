package tfg.uniovi.melodies.entities

import com.google.firebase.Timestamp

class Folder(
    val name: String, //max 30 chars
    val color: Int,
    val creationTime: Timestamp,
    val folderId: String
) {

}