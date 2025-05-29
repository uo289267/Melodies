package tfg.uniovi.melodies.tools.pitchdetector

import android.util.Log
import tfg.uniovi.melodies.entities.notes.NoteDominant
import tfg.uniovi.melodies.entities.notes.ScoreElement

class SheetChecker () {
    val notes =
        arrayOf("C", "D", "E", "F", "G", "A", "B")

    /**
     * Returns true if the noteToCheck has been played
     *          false if the noteToCheck was not played
     *          null if there was no dominant note when listening
     */
    fun isNotePlayedCorrectly(noteToCheck : ScoreElement,
                              samplingIntervalMs: Long = 1000L,
                              dominancePercentage : Double = 0.95): Boolean? {

        val listOfNotes = mutableListOf<String>()
        val start = System.currentTimeMillis()
        val durationMs = noteToCheck.getDuration()

        // Listening for the amount of time said in samplingInterval
        while (System.currentTimeMillis() - start < durationMs) {
            val note = PitchDetector.getLastDetectedNote()
            listOfNotes.add(note)
            println("Escuchando nota: $note")
            Thread.sleep(samplingIntervalMs)
        }

        // Get the frequency of each note
        val noteCounts = listOfNotes.groupingBy { it }.eachCount()
        val totalSamples = listOfNotes.size
        val threshold = (totalSamples * dominancePercentage).toInt()

        // find the note that appears at least 95%
        val dominantNote = noteCounts.entries.find { it.value >= threshold }?.key

        Log.d("SHEETcHECKER", "Esperado: , Detectado: $dominantNote")

        return dominantNote?.let { noteToCheck.check(NoteDominant(notaBase(it)))  }
    }

    fun notaBase(nota: String): Char {
        return nota[0]
    }

}