package tfg.uniovi.melodies.entities

import org.w3c.dom.Document
import tfg.uniovi.melodies.utils.parser.String2MusicXML
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Represents a MusicXML sheet stored in the application.
 *
 * A MusicXMLSheet holds metadata about the score (name, author, identifiers)
 * and the actual MusicXML content, parsed into a DOM [Document] for further processing.
 *
 * @property name The title of the sheet music.
 * @property stringSheet The MusicXML content as a raw string.
 * @property author The composer or arranger of the sheet music.
 * @property id Firebase unique identifier for the sheet.
 * @property folderId Firebase unique Identifier of the folder where the sheet is stored.
 * @property musicxml Parsed MusicXML content as an XML [Document].
 */
class MusicXMLSheet(
    val name: String,
    val stringSheet: String,
    val author: String,
    val id: String,
    val folderId: String
) {
    /**
    * The parsed MusicXML content, converted from [stringSheet]
    * into a [Document] for structured access to its elements.
    */
    val musicxml: Document = String2MusicXML.string2doc(stringSheet)
}