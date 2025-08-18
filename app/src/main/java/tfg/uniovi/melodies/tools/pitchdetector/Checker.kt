package tfg.uniovi.melodies.tools.pitchdetector



import android.util.Log
import tfg.uniovi.melodies.entities.notes.NoteDominant
import tfg.uniovi.melodies.entities.notes.interfaces.ScoreElement
import tfg.uniovi.melodies.utils.parser.XMLParser

private const val DURATION_PERCENTAGE = 0.95

/**
 * SheetChecker2 is responsible for verifying if a played note matches the expected
 * score element in both onset (start of the note) and duration.
 *
 * This class relies on PitchDetector to capture detected notes in real-time,
 * and validates if the detected onset and duration correspond to the expected score.
 */
class SheetChecker2 {
    val notes = arrayOf("C", "D", "E", "F", "G", "A", "B")

    /**
     * Verifies if a note was played correctly, first by checking the onset,
     * then by checking if it was held for the expected duration.
     *
     * @param noteToCheck The score element to verify.
     * @param dominancePercentage Minimum percentage of samples that must match
     *                            the expected note during its duration.
     * @param onsetTimeoutMs Maximum time to wait for the onset.
     * @return True if the note was correctly played,
     *         False if it was incorrect,
     *         or Null if the onset timed out.
     */
    fun isNotePlayedCorrectlyWithOnset(noteToCheck: ScoreElement,
                                       dominancePercentage: Double,
                                       onsetTimeoutMs: Long): Boolean {



        Log.d("SHEET_CHECKER", "=== INICIANDO VERIFICACIÓN DE NOTA: $noteToCheck ===")

        // 1: Wait for the note to be on set meaning it is starting to be played
        val onsetDetected = waitForExpectedNoteOnset(noteToCheck, onsetTimeoutMs)

        // 2: Once it is onset verify the duration
        return if(onsetDetected==true)
            isNotePlayedForCorrectDuration(noteToCheck, dominancePercentage)
        else
            false
    }

    /**
     * Waits for the expected note to be played (onset).
     *
     * @param noteToCheck The score element that is expected to be played.
     * @param timeoutMs Maximum waiting time in milliseconds before giving up.
     * @return True if the expected note onset is detected,
     *         False if a different note onset is detected,
     *         or Null if the timeout expires without detection.
     */
    private fun waitForExpectedNoteOnset(noteToCheck: ScoreElement,
                                         timeoutMs: Long = 10000L): Boolean? {
        val startTime = System.currentTimeMillis()

        Log.d("SHEET_CHECKER", "Esperando inicio de nota: $noteToCheck")

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val detectedOnset = PitchDetector.getLastDetectedNote()

            if(detectedOnset != PitchDetector.SILENCE){
                Log.d("SHEET_CHECKER", "Onset detectado: $detectedOnset, Esperado: $noteToCheck")
                return noteToCheck.check(NoteDominant(baseNote(detectedOnset), octave(detectedOnset)))
            }
        }

        Log.d("SHEET_CHECKER", "Timeout esperando nota: $noteToCheck")
        return null // Timeout
    }


    /**
     * Verifies if a note was held for a correct percentage of its expected duration.
     *
     * @param noteToCheck The score element to verify.
     * @param dominancePercentage Minimum percentage of samples that must match
     *                            the expected note.
     * @return True if the note matches the expected one for most of its duration,
     *         False otherwise.
     */
    private fun isNotePlayedForCorrectDuration(
        noteToCheck: ScoreElement,
        dominancePercentage: Double
    ): Boolean {
        val listOfNotes = mutableListOf<String>()
        val start = System.currentTimeMillis()
        val durationMs = noteToCheck.getDuration() * DURATION_PERCENTAGE

        Log.d("SHEET_CHECKER", "Verificando nota durante ${durationMs}ms...")

        while (System.currentTimeMillis() - start < durationMs) {
            val note = PitchDetector.getLastDetectedNote()
            listOfNotes.add(note)
            Thread.sleep(50)
        }

        // Calculate dominance as in the original method
        val noteCounts = listOfNotes.groupingBy { it }.eachCount()
        val totalSamples = listOfNotes.size
        val threshold = (totalSamples * dominancePercentage).toInt()

        // Find the note that appears at least the required percentage
        val dominantNote = noteCounts.entries.find { it.value >= threshold }?.key

        Log.d("SHEET_CHECKER", "Muestras totales: $totalSamples, Umbral: $threshold")
        Log.d("SHEET_CHECKER", "Conteos de notas: $noteCounts")
        Log.d("SHEET_CHECKER", "Nota dominante: $dominantNote, Esperada: $noteToCheck")

        val result: Boolean = if (dominantNote != null)
            noteToCheck.check(NoteDominant(baseNote(dominantNote), octave(dominantNote)))
        else
            noteToCheck.check(null)

        Log.d("SHEET_CHECKER", "=== RESULTADO FINAL: $result ===")

        return result
    }
    /**
     * Extracts the base note (e.g., 'C' from "C4").
     *
     * @param note The note string (e.g., "C4").
     * @return The base note character.
     */
    private fun baseNote(note: String): Char {
        return note[0]
    }

    /**
     * Extracts the octave from a note string (e.g., '4' from "C4").
     *
     * @param note The note string.
     * @return The octave number, or the default from XMLParser if not found.
     */
    private fun octave(note: String): Int {
        return Regex("\\d+").find(note)?.value?.toInt() ?: XMLParser.BASE_OCTAVE_FLUTE
    }
}