package tfg.uniovi.melodies.tools.pitchdetector

import android.util.Log
import kotlin.random.Random

class SheetChecker () {
    val solfegeNotes =
        arrayOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Si")
    var noteToPlay ="Do"
    fun getNotesToPlay(): String {
        val num = Random.nextInt(solfegeNotes.size)
        noteToPlay = solfegeNotes[num]
        return solfegeNotes[num]
    }
    fun isNotePlayedCorrectly(): Boolean {
        val listOfNotes = mutableListOf<String>()
        val start = System.currentTimeMillis()
        val samplingIntervalMs = 100L
        val durationMs = 1000L

        // Escuchar durante 1 segundo (1000 ms)
        while (System.currentTimeMillis() - start < durationMs) {
            val note = PitchDetector.getLastDetectedNote()
            listOfNotes.add(note)
            println("Escuchando nota: $note")
            Thread.sleep(samplingIntervalMs)
        }

        // Contar frecuencia de cada nota
        val noteCounts = listOfNotes.groupingBy { it }.eachCount()
        val totalSamples = listOfNotes.size
        val threshold = (totalSamples * 0.95).toInt()

        // Buscar la nota que aparece al menos en el 95% de los casos
        val dominantNote = noteCounts.entries.find { it.value >= threshold }?.key

        Log.d("SHEETcHECKER", "Esperado: $noteToPlay, Detectado: $dominantNote")

        return dominantNote?.let { notaBase(it) } == noteToPlay
    }



    fun notaBase(nota: String): String {
        return when {
            nota.length >= 4 && nota[3] == '#' -> nota.substring(0, 4) // ej: "Sol#7" → "Sol#"
            nota.length >= 3 && nota.substring(0, 3) in listOf("Sol", "La#", "Si#", "Do#", "Re#", "Fa#", "Mi#") -> nota.substring(0, 3)
            else -> nota.substring(0, 2) // ej: "Si6", "La5"
        }
    }
}