package tfg.uniovi.melodies.entities

import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class MusicXMLSheet(val name: String, stringSheet: String) {

    val musicxml: Document = stringToDocument(stringSheet)

    private fun stringToDocument(xmlString: String): Document {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            builder.parse(ByteArrayInputStream(xmlString.toByteArray(Charsets.UTF_8)))
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing XML: ${e.message}", e)
        }
    }
}