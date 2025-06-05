package tfg.uniovi.melodies

import org.junit.Test
import org.w3c.dom.Document
import tfg.uniovi.melodies.utils.parser.XMLParser
import java.io.ByteArrayInputStream
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import tfg.uniovi.melodies.entities.notes.Note

class XMLParserTests {

    @Test
    fun creationOfOneNoteTest(){
        val doc = document("C4.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)
        val note = notes[0] as Note
        assertEquals('C', note.getName())
        assertEquals(4, note.getOctave())
        assertEquals(false, note.getSharp())

    }

    @Test
    fun creationOfTwoNotesTest(){
        val doc = document("C4G5.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(2, notes.size)
        //first note C4
        val note = notes[0] as Note
        assertEquals('C', note.getName())
        assertEquals(4, note.getOctave())
        assertEquals(false, note.getSharp())
        //second note G5
        val note2 = notes[1] as Note
        assertEquals('G', note2.getName())
        assertEquals(5, note2.getOctave())
        assertEquals(false, note2.getSharp())
    }

    @Test
    fun creationOfOneSharpNoteTest(){
        val doc = document("A3sharp.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)
        val note = notes[0] as Note
        assertEquals('A', note.getName())
        assertEquals(3, note.getOctave())
        assertEquals(true, note.getSharp())
    }

    @Test
    fun durationOfQuarterNote(){
        val doc = document("C4.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)
        val noteDuration = 256
        val BPM = 100
        val relativeValue = 1
        val divisionsPerQuarter = 256

        val note = notes[0] as Note

        assertEquals((noteDuration/divisionsPerQuarter*(60_000 / BPM) * relativeValue).toLong(), note.getDuration())
    }

    private fun document(fileName: String): Document? {
        val classLoader = javaClass.classLoader
        val file = File(classLoader!!.getResource(fileName)!!.file)

        val factory = DocumentBuilderFactory.newInstance()
        factory.isValidating = false
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(file) // o InputStream
        return doc
    }


}