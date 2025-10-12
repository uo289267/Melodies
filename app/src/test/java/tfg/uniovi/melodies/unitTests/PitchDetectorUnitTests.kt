package tfg.uniovi.melodies.unitTests
import org.junit.jupiter.api.Test
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

class PitchDetectorUnitTests {
    @Test
    fun `convertFrequencyToNote returns correct notes`() {
        val method = PitchDetector::class.declaredMemberFunctions
            .first { it.name == "convertFrequencyToNote" }
        method.isAccessible = true

        val noteA4 = method.call(PitchDetector, 440f) as String
        assertEquals("A4", noteA4)

        val noteC4 = method.call(PitchDetector, 261.63f) as String
        assertEquals("C4", noteC4)

        val noteE5 = method.call(PitchDetector, 659.25f) as String
        assertEquals("E5", noteE5)

        val noteFs3 = method.call(PitchDetector, 185f) as String
        assertEquals("F#3", noteFs3)
    }
    @Test
    fun `convertFrequencyToNote returns correct chromatic scale for octave 4`() {
        val method = PitchDetector::class.declaredMemberFunctions
            .first { it.name == "convertFrequencyToNote" }
        method.isAccessible = true

        // Lista de notas en la octava 4 y sus frecuencias aproximadas
        val notesAndFrequencies = mapOf(
            "C4" to 261.63f,
            "C#4" to 277.18f,
            "D4" to 293.66f,
            "D#4" to 311.13f,
            "E4" to 329.63f,
            "F4" to 349.23f,
            "F#4" to 369.99f,
            "G4" to 392.00f,
            "G#4" to 415.30f,
            "A4" to 440.00f,
            "A#4" to 466.16f,
            "B4" to 493.88f
        )

        for ((note, freq) in notesAndFrequencies) {
            val detectedNote = method.call(PitchDetector, freq) as String
            assertEquals(note, detectedNote, "Frequency $freq Hz should map to $note")
        }
    }

    @Test
    fun `convertFrequencyToNote returns correct notes across multiple octaves`() {
        val method = PitchDetector::class.declaredMemberFunctions
            .first { it.name == "convertFrequencyToNote" }
        method.isAccessible = true

        val testNotes = mapOf(
            "C0" to 16.35f, "C#0" to 17.32f, "D0" to 18.35f, "D#0" to 19.45f,
            "E0" to 20.60f, "F0" to 21.83f, "F#0" to 23.12f, "G0" to 24.50f,
            "G#0" to 25.96f, "A0" to 27.50f, "A#0" to 29.14f, "B0" to 30.87f,

            "C1" to 32.70f, "C#1" to 34.65f, "D1" to 36.71f, "D#1" to 38.89f,
            "E1" to 41.20f, "F1" to 43.65f, "F#1" to 46.25f, "G1" to 49.00f,
            "G#1" to 51.91f, "A1" to 55.00f, "A#1" to 58.27f, "B1" to 61.74f,

            "C2" to 65.41f, "C#2" to 69.30f, "D2" to 73.42f, "D#2" to 77.78f,
            "E2" to 82.41f, "F2" to 87.31f, "F#2" to 92.50f, "G2" to 98.00f,
            "G#2" to 103.83f, "A2" to 110.00f, "A#2" to 116.54f, "B2" to 123.47f
        )

        for ((note, freq) in testNotes) {
            val detectedNote = method.call(PitchDetector, freq) as String
            assertEquals(note, detectedNote, "Frequency $freq Hz should map to $note")
        }
    }

    @Test
    fun `convertFrequencyToNote returns correct note for frequencies near boundaries`() {
        val method = PitchDetector::class.declaredMemberFunctions
            .first { it.name == "convertFrequencyToNote" }
        method.isAccessible = true

        val noteB4 = method.call(PitchDetector, 493.88f) as String
        assertEquals("B4", noteB4)

        val noteC5 = method.call(PitchDetector, 523.25f) as String
        assertEquals("C5", noteC5)
    }
}