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

class XMLParser() {
    private lateinit var musicxml: Document
    private val notes = mutableListOf<ScoreElement>()
    private var quarterNoteDuration : Long? = null
    private var divisionsPerQuarter: Int? = null

    constructor(musicxml: Document) : this() {
        this.musicxml = musicxml
    }

    /**
     * Parses the musicXML given by constructor to create Note objects
     */
    fun parseAllNotes() {
        try {
            this.quarterNoteDuration = getQuarterNoteDurationMsFromMetronome()
            this.divisionsPerQuarter = getDivisionsPerQuarter()

            val notes: NodeList = musicxml.getElementsByTagName("note")
            var noteCount = 0
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
                    val rest = Rest(noteCount++, durationMs.toLong())
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
                    //From standard 4 to 5 as flute plays in 4 or 6 TODO
                    if(octave == 4)
                        octave = 5
                    else if(octave == 5)
                        octave = 6
                    var sharp = false
                    if (alter != null) {
                        val alterElement = alter as Element
                        if (alterElement.textContent.toInt() == 1)
                            sharp = true
                    }

                    val note = Note(noteCount++, durationMs.toLong(), name, octave,sharp)

                    for (rest in pendingRests) {
                        rest.setFollowingNote(note)
                    }
                    pendingRests.clear()

                    this.notes.add(note)
                }
            }
        }catch (e: NullPointerException){
            throw SVGParserException("MusicXML badly written and missing elements or attributes")
        }
    }


    private fun getQuarterNoteDurationMsFromMetronome(): Long? {
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

    private fun getDivisionsPerQuarter(): Int? {
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

    private fun calculateNoteDurationMs(
        noteDuration: Int
    ): Int {
        return ((noteDuration.toDouble() / this.divisionsPerQuarter!!) * this.quarterNoteDuration!!).toInt()
    }

    fun getAllNotes(): List<ScoreElement> {
        return this.notes
    }

    fun getTotalNumberOfNotes(): Int {
        return this.notes.size
    }
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

    fun findNameTitle(context: Context, xmlDocument: Document) : String{
        val workNodes = xmlDocument.getElementsByTagName("work-title")
        if (workNodes.length > 0) {
            return workNodes.item(0).textContent
        }
        return getString(context, R.string.unknown_name)

    }

}
