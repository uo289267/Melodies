package tfg.uniovi.melodies.utils.parser

import android.content.Context
import androidx.core.content.ContextCompat.getString
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.notes.Note
import tfg.uniovi.melodies.entities.notes.interfaces.ScoreElement
import tfg.uniovi.melodies.entities.notes.Rest
/**
 * Parser for MusicXML documents that extracts notes and metadata.
 *
 * Converts a MusicXML [Document] into a list of [ScoreElement] objects,
 * handling standard notes and rests. Also provides utility functions to
 * retrieve the author and title of a composition.
 *
 * **Notes about octaves:**
 * - All input sheets are expected to have a base octave of 4.
 */
private const val NEUTRAL_OCTAVE: Int = 4
class XMLParser() {
    //ALL SHEETS PROVIDED NEED TO HAVE THEIR BASE OCTAVE TO BE 4
    companion object {
        const val BASE_OCTAVE_FLUTE: Int = 5
    }


    private lateinit var musicxml: Document
    private val notes = mutableListOf<ScoreElement>()
    internal var quarterNoteDuration : Long? = null
    internal var divisionsPerQuarter: Int? = null


    constructor(musicxml: Document) : this() {
        this.musicxml = musicxml
    }

    /**
     * Parses all notes and rests from the MusicXML document.
     *
     * - Converts durations to milliseconds.
     * - Handles rests and sets references to the following note.
     * - Transposes octaves for instruments (e.g., flute) if necessary.
     *
     * @throws XMLParserException If required MusicXML elements or attributes are missing.
     */
    fun parseAllNotes() {
        try {
            this.quarterNoteDuration = getQuarterNoteDurationMsFromMetronome()
            this.divisionsPerQuarter = fetchDivisionsPerQuarter()

            val notes: NodeList = musicxml.getElementsByTagName("note")
            val pendingRests = mutableListOf<Rest>()

            for (i in 0 until notes.length) {
                val noteElement = notes.item(i) as Element
                val restList = noteElement.getElementsByTagName("rest")
                val durationList = noteElement.getElementsByTagName("duration")
                val durationElement = durationList.item(0) as Element
                val duration = durationElement.textContent.toInt()
                val durationMs = calculateNoteDurationMs(duration)

                val isRest = restList.length > 0
                if (isRest) {
                    val rest = Rest(durationMs.toLong())
                    pendingRests.add(rest)
                    this.notes.add(rest)
                    continue
                }

                val pitchList = noteElement.getElementsByTagName("pitch")
                if (pitchList.length > 0) {
                    val pitchElement = pitchList.item(0) as Element
                    val stepElement = pitchElement.getElementsByTagName("step").item(0) as Element
                    val octaveElement =
                        pitchElement.getElementsByTagName("octave").item(0) as Element
                    val alter = pitchElement.getElementsByTagName("alter").item(0)

                    val name = stepElement.textContent[0]
                    var octave = octaveElement.textContent.toInt()
                    //From standard 4 to 5 as flute plays in 4 or 6
                    octave = if(octave == NEUTRAL_OCTAVE)
                        BASE_OCTAVE_FLUTE
                    else{
                        octave-NEUTRAL_OCTAVE+BASE_OCTAVE_FLUTE
                    }
                    var sharp = false
                    if (alter != null) {
                        val alterElement = alter as Element
                        if (alterElement.textContent.toInt() == 1)
                            sharp = true
                    }

                    val note = Note(durationMs.toLong(), name, octave,sharp)

                    for (rest in pendingRests) {
                        rest.setFollowingNote(note)
                    }
                    pendingRests.clear()

                    this.notes.add(note)
                }
            }
        }catch (e: NullPointerException){
            throw XMLParserException("MusicXML badly written and missing elements or attributes")
        }
    }

    /**
     * Calculates the duration of a quarter note in milliseconds from metronome markings.
     *
     * @return Duration of a quarter note in milliseconds, or `null` if not found.
     */
    internal fun getQuarterNoteDurationMsFromMetronome(): Long? {
        val beatUnitMap = mapOf(
            "whole" to 4.0,
            "half" to 2.0,
            "quarter" to 1.0,
            "eighth" to 0.5,
            "16th" to 0.25,
            "32nd" to 0.125,
            "64th" to 0.0625,
            "128th" to 0.03125
        )

        val directions = this.musicxml.getElementsByTagName("direction")
        for (i in 0 until directions.length) {
            val direction = directions.item(i) as Element
            val directionTypes = direction.getElementsByTagName("direction-type")
            for (j in 0 until directionTypes.length) {
                val dirType = directionTypes.item(j) as Element
                val metronomes = dirType.getElementsByTagName("metronome")
                for (k in 0 until metronomes.length) {
                    val metronome = metronomes.item(k) as Element
                    val beatUnitElement = metronome.getElementsByTagName("beat-unit").item(0)
                    val perMinuteElement = metronome.getElementsByTagName("per-minute").item(0)

                    if (beatUnitElement != null && perMinuteElement != null) {
                        val beatUnit = beatUnitElement.textContent.trim()
                        val bpm = perMinuteElement.textContent.trim().toDoubleOrNull()

                        val hasDot = metronome.getElementsByTagName("beat-unit-dot").length > 0
                        val relativeValue = beatUnitMap[beatUnit]?.let { if (hasDot) it * 1.5 else it }
                        if (relativeValue != null && bpm != null) {
                            val quarterNoteDurationMs = ((60_000 / bpm) * relativeValue).toLong()
                            return quarterNoteDurationMs
                        }
                    }
                }
            }
        }
        return null
    }
    /**
     * Retrieves the number of divisions per quarter note from the MusicXML document.
     *
     * @return Number of divisions per quarter note, or `null` if not found.
     */
    internal fun fetchDivisionsPerQuarter(): Int? {
        val measures = musicxml.getElementsByTagName("measure")
        for (i in 0 until measures.length) {
            val measure = measures.item(i) as Element
            val attributesList = measure.getElementsByTagName("attributes")
            for (j in 0 until attributesList.length) {
                val attributes = attributesList.item(j) as Element
                val divisionsList = attributes.getElementsByTagName("divisions")
                if (divisionsList.length > 0) {
                    return divisionsList.item(0).textContent.trim().toIntOrNull()
                }
            }
        }
        return null
    }
    /**
     * Calculates the duration of a note in milliseconds based on divisions and quarter note duration.
     *
     * @param noteDuration Duration of the note in MusicXML divisions.
     * @return Duration in milliseconds.
     */
    internal fun calculateNoteDurationMs(
        noteDuration: Int
    ): Int {
        return ((noteDuration.toDouble() / this.divisionsPerQuarter!!) * this.quarterNoteDuration!!).toInt()
    }
    /**
     * Returns all parsed [ScoreElement]s (notes and rests).
     *
     * @return List of [ScoreElement].
     */
    fun getAllNotes(): List<ScoreElement> {
        return this.notes
    }
    /**
     * Finds the composer/author of the MusicXML document.
     *
     * @param context The application context, used to access default string resources.
     * @param xmlDocument The MusicXML document.
     * @return Composer name, or "Anonymous" if not specified.
     */
    fun findAuthor(context: Context, xmlDocument: Document) : String{
        val nodeList = xmlDocument.getElementsByTagName("creator")
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i) as Element
            if (node.getAttribute("type") == "composer") {
                return node.textContent
            }
        }
        return getString(context, R.string.anonymous)
    }
    /**
     * Finds the title of the piece in the MusicXML document.
     *
     * @param context The application context, used to access default string resources.
     * @param xmlDocument The MusicXML document.
     * @return Title of the piece, or "Unknown Name" if not specified.
     */
    fun findNameTitle(context: Context, xmlDocument: Document) : String{
        val workNodes = xmlDocument.getElementsByTagName("work-title")
        if (workNodes.length > 0) {
            return workNodes.item(0).textContent
        }
        return getString(context, R.string.unknown_name)

    }

}
