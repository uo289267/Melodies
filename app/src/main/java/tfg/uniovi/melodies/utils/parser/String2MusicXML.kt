package tfg.uniovi.melodies.utils.parser

import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object String2MusicXML {
    fun string2doc(xmlString: String) : Document{
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            builder.parse(ByteArrayInputStream(xmlString.toByteArray(Charsets.UTF_8)))
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing XML: ${e.message}", e)
        }
    }
}