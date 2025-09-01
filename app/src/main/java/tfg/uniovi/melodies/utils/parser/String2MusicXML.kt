package tfg.uniovi.melodies.utils.parser

import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
/**
 * Utility object for converting a MusicXML string into a [Document] object.
 *
 * This is useful when loading MusicXML content from a string instead of a file.
 */
object String2MusicXML {
    /**
     * Parses a MusicXML string into a [Document].
     *
     * @param xmlString The MusicXML content as a [String].
     * @return A [Document] representing the parsed XML.
     * @throws IllegalArgumentException If the string cannot be parsed as valid XML.
     */
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