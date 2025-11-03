package tfg.uniovi.melodies.unitTests

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import tfg.uniovi.melodies.model.notes.comparables.Note
import tfg.uniovi.melodies.model.notes.rest.Rest
import tfg.uniovi.melodies.processing.parser.XMLParser
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XMLParserTests {

    @Test
    fun creationOfOneNoteTest() {
        val doc = document("C4.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)
        val note = notes[0] as Note
        assertEquals('C', note.name)
        assertEquals(5, note.octave)

        assertEquals(false, note.sharp)
    }

    @Test
    fun creationOfOneRestTest() {
        val doc = document("rest.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)
        val rest = notes[0] as Rest
        assertEquals(1200, rest.getDuration())
        assertEquals(null, rest.getFollowingNote())
    }
    @Test
    fun creationOfOneRestAndNoteTest() {
        val doc = document("restC4.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(2, notes.size)
        val rest = notes[0] as Rest
        val note = notes[1] as Note
        assertEquals(1200, rest.getDuration())
        assertEquals(note, rest.getFollowingNote())
    }

    @Test
    fun creationOfTwoNotesTest() {
        val doc = document("C4G5.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(2, notes.size)

        val note1 = notes[0] as Note
        assertEquals('C', note1.name)
        assertEquals(5, note1.octave)
        assertEquals(false, note1.sharp)

        val note2 = notes[1] as Note
        assertEquals('G', note2.name)
        assertEquals(6, note2.octave)
        assertEquals(false, note2.sharp)
    }

    @Test
    fun creationOfOneSharpNoteTest() {
        val doc = document("A3sharp.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)
        val note = notes[0] as Note
        assertEquals('A', note.name)
        assertEquals(4, note.octave)
        assertEquals(true, note.sharp)
    }

    @Test
    fun durationOfQuarterNote() {
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
        assertEquals(
            (noteDuration / divisionsPerQuarter * (60_000 / BPM) * relativeValue).toLong(),
            note.getDuration()
        )
    }
    @Test
    fun durationOfHalfNoteTest() {
        val doc = document("C4Half.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)

        val note = notes[0] as Note
        val noteDuration = 512
        val BPM = 100
        val relativeValue = 1
        val divisionsPerQuarter = 256

        assertEquals(
            (noteDuration / divisionsPerQuarter * (60_000 / BPM) * relativeValue).toLong(),
            note.getDuration()
        )
    }

    @Test
    fun durationOfHalfNoteWithNewDivisionsTest() {
        val doc = document("C4Half_1024.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val notes = parser.getAllNotes()
        assertEquals(1, notes.size)

        val note = notes[0] as Note
        val noteDuration = 1024
        val BPM = 100
        val relativeValue = 1
        val divisionsPerQuarter = 1024

        assertEquals(
            (noteDuration / divisionsPerQuarter * (60_000 / BPM) * relativeValue).toLong(),
            note.getDuration()
        )
    }
    @Test
    fun durationOfEighthNoteTest() {
        val doc = document("C4Eighth.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val note = parser.getAllNotes()[0] as Note
        val noteDuration = 128 // divisiones para corchea
        val BPM = 100
        val divisionsPerQuarter = 256

        assertEquals(
            (noteDuration.toDouble() / divisionsPerQuarter * (60_000 / BPM)).toLong(),
            note.getDuration()
        )
    }

    @Test
    fun durationAtDifferentBPMTest() {
        val doc = document("C4_bpm.xml")
        val parser = XMLParser(doc!!)
        parser.parseAllNotes()
        val note = parser.getAllNotes()[0] as Note
        val BPM = 60
        val noteDuration = 256
        val divisionsPerQuarter = 256

        assertEquals(
            (noteDuration / divisionsPerQuarter * (60_000 / BPM)).toLong(),
            note.getDuration()
        )
    }

    @Test
    fun metronomeNotFoundXMLTest() {
        val doc = document("metronomeNotFound.xml")
        val parser = XMLParser(doc!!)

        val quarterDuration = parser.getQuarterNoteDurationMsFromMetronome()

        assertEquals(null, quarterDuration)
    }

    @Test
    fun directionTypeNotFoundXMLTest() {
        val doc = document("directionTypeNotFound.xml")
        val parser = XMLParser(doc!!)

        val quarterDuration = parser.getQuarterNoteDurationMsFromMetronome()

        assertEquals(null, quarterDuration)
    }
    @Test
    fun directionNotFoundXMLTest() {
        val doc = document("directionNotFound.xml")
        val parser = XMLParser(doc!!)

        val quarterDuration = parser.getQuarterNoteDurationMsFromMetronome()

        assertEquals(null, quarterDuration)
    }
    @Test
    fun beatUnitNotFoundXMLTest() {
        val doc = document("beatUnitNotFound.xml")
        val parser = XMLParser(doc!!)

        val quarterDuration = parser.getQuarterNoteDurationMsFromMetronome()

        assertEquals(null, quarterDuration)
    }
    @Test
    fun perMinuteNotFoundXMLTest() {
        val doc = document("perMinuteNotFound.xml")
        val parser = XMLParser(doc!!)

        val quarterDuration = parser.getQuarterNoteDurationMsFromMetronome()

        assertEquals(null, quarterDuration)
    }
    @Test
    fun divisionsNotFoundXMLTest() {
        val doc = document("C4DivisionsNotFound.xml") // XML sin <divisions>
        val parser = XMLParser(doc!!)

        val divisions = parser.fetchDivisionsPerQuarter()

        assertEquals(null, divisions)
    }

    @Test
    fun attributesNotFoundXMLTest() {
        val doc = document("C4AtributesNotFound.xml") // XML donde <attributes> falta
        val parser = XMLParser(doc!!)

        val divisions = parser.fetchDivisionsPerQuarter()

        assertEquals(null, divisions)
    }

    @Test
    fun measureNotFoundXMLTest() {
        val doc = document("C4MeasureNotFound.xml") // XML donde <measure> falta
        val parser = XMLParser(doc!!)

        val divisions = parser.fetchDivisionsPerQuarter()

        assertEquals(null, divisions)
    }

    private fun document(fileName: String): Document? {
        val classLoader = javaClass.classLoader
        val file = File(classLoader!!.getResource(fileName)!!.file)
        val factory = DocumentBuilderFactory.newInstance()
        factory.isValidating = false
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        val builder = factory.newDocumentBuilder()
        return builder.parse(file)
    }

}
