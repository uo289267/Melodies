package tfg.uniovi.melodies.entities

import org.w3c.dom.Document
import tfg.uniovi.melodies.utils.parser.String2MusicXML
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class MusicXMLSheet(
    val name: String,
    val stringSheet: String,
    val author: String,
    val id: String,
    val folderId: String
) {

    val musicxml: Document = String2MusicXML.string2doc(stringSheet)

}