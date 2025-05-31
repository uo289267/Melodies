package tfg.uniovi.melodies.entities

import com.google.firebase.Timestamp

class Folder(
    val name: String, //max 30 chars
    val color: Colors,
    val creationTime: Timestamp, //TODO necesario?
    val folderId: String
){
    override fun toString(): String {
        return this.name
    }
}
enum class Colors{
    YELLOW, PINK, BLUE
}