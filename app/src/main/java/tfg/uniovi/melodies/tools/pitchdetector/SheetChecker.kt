package tfg.uniovi.melodies.tools.pitchdetector

import android.util.Log
import tfg.uniovi.melodies.entities.notes.Note
import tfg.uniovi.melodies.entities.notes.NoteDominant
import tfg.uniovi.melodies.entities.notes.interfaces.ScoreElement

class SheetChecker () {
    val notes =
        arrayOf("C", "D", "E", "F", "G", "A", "B")
    // Variables para detección de onset
    private var lastDetectedNote: String? = null
    private var lastNoteChangeTime: Long = 0
    private val minimumNoteStabilityMs = 100L // Tiempo mínimo para considerar una nota estable

    /**
     * Detecta si una nueva nota ha comenzado a sonar
     * @return la nota detectada si es un onset, null si no hay cambio
     */
    private fun detectNoteOnset(): String? {
        val currentNote = PitchDetector.getLastDetectedNote()
        val currentTime = System.currentTimeMillis()

        // Si no hay nota detectada (silencio)
        if (currentNote.isEmpty() || currentNote == "Desconocido") {
            return null
        }

        // Si es la primera detección o cambió la nota
        if (lastDetectedNote != currentNote) {
            // Verificar que ha pasado suficiente tiempo desde el último cambio
            if (currentTime - lastNoteChangeTime > minimumNoteStabilityMs) {
                lastDetectedNote = currentNote
                lastNoteChangeTime = currentTime
                Log.d("ONSET_DETECTOR", "Nueva nota detectada: $currentNote")
                return currentNote
            }
        }

        return null
    }

    /**
     * Espera a detectar el inicio de la nota esperada
     * @param noteToCheck La nota que esperamos
     * @param timeoutMs Tiempo máximo de espera
     * @return true si detecta la nota esperada, false si detecta otra, null si timeout
     */
    private fun waitForExpectedNoteOnset(noteToCheck: ScoreElement, timeoutMs: Long = 10000L): Boolean? {
        val startTime = System.currentTimeMillis()

        var expectedNoteName = "Z"
        if (noteToCheck is Note) {
            expectedNoteName = noteToCheck.name.toString() + noteToCheck.octave.toString()
        }

        Log.d("SHEET_CHECKER", "Esperando inicio de nota: $expectedNoteName")

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val detectedOnset = detectNoteOnset()

            detectedOnset?.let { detectedNote ->
                Log.d("SHEET_CHECKER", "Onset detectado: $detectedNote, Esperado: $expectedNoteName")

                // Verificar si la nota detectada coincide con la esperada
                val isCorrect = noteToCheck.check(NoteDominant(notaBase(detectedNote), octave(detectedNote)))
                return isCorrect
            }

            // Pequeña pausa para no sobrecargar el procesador
            Thread.sleep(50)
        }

        Log.d("SHEET_CHECKER", "Timeout esperando nota: $expectedNoteName")
        return null // Timeout
    }

    /**
     * NUEVO MÉTODO: Primero detecta el onset, luego verifica durante la duración
     * @param noteToCheck La nota que esperamos
     * @param dominancePercentage Porcentaje mínimo de dominancia (por defecto 0.95)
     * @param onsetTimeoutMs Tiempo máximo para esperar el onset (por defecto 10 segundos)
     * @return true si nota correcta, false si incorrecta, null si no se detectó onset
     */
    fun isNotePlayedCorrectlyWithOnset(noteToCheck: ScoreElement,
                                       dominancePercentage: Double = 0.95,
                                       onsetTimeoutMs: Long = 10000L): Boolean? {

        var expectedNoteName = "Z"
        if (noteToCheck is Note) {
            expectedNoteName = noteToCheck.name.toString() + noteToCheck.octave.toString()
        }

        Log.d("SHEET_CHECKER", "=== INICIANDO VERIFICACIÓN DE NOTA: $expectedNoteName ===")

        // PASO 1: Esperar a que comience la nota esperada
        val onsetDetected = waitForExpectedNoteOnset(noteToCheck, onsetTimeoutMs)

        when (onsetDetected) {
            null -> {
                Log.d("SHEET_CHECKER", "TIMEOUT: No se detectó inicio de nota $expectedNoteName")
                return null
            }
            false -> {
                Log.d("SHEET_CHECKER", "NOTA INCORRECTA detectada al inicio para $expectedNoteName")
                return false
            }
            true -> {
                Log.d("SHEET_CHECKER", "✓ ONSET CORRECTO detectado para $expectedNoteName. Iniciando verificación de duración...")
            }
        }

        // PASO 2: Una vez detectado el onset correcto, verificar durante la duración
        val listOfNotes = mutableListOf<String>()
        val start = System.currentTimeMillis()
        val durationMs = noteToCheck.getDuration()

        Log.d("SHEET_CHECKER", "Verificando nota durante ${durationMs}ms...")

        // Aquí está tu bucle original, pero que se ejecuta DESPUÉS del onset
        while (System.currentTimeMillis() - start < durationMs) {
            val note = PitchDetector.getLastDetectedNote()
            listOfNotes.add(note)
            Thread.sleep(50) // Reducido el sleep para mejor responsividad
        }

        // PASO 3: Calcular dominancia como en tu método original
        val noteCounts = listOfNotes.groupingBy { it }.eachCount()
        val totalSamples = listOfNotes.size
        val threshold = (totalSamples * dominancePercentage).toInt()

        // Encontrar la nota que aparece al menos el porcentaje requerido
        val dominantNote = noteCounts.entries.find { it.value >= threshold }?.key

        Log.d("SHEET_CHECKER", "Muestras totales: $totalSamples, Umbral: $threshold")
        Log.d("SHEET_CHECKER", "Conteos de notas: $noteCounts")
        Log.d("SHEET_CHECKER", "Nota dominante: $dominantNote, Esperada: $expectedNoteName")

        val result = dominantNote?.let {
            noteToCheck.check(NoteDominant(notaBase(it), octave(it)))
        }

        Log.d("SHEET_CHECKER", "=== RESULTADO FINAL: ${result ?: "null"} ===")

        return result
    }

    /*
        /**
         * Returns true if the noteToCheck has been played
         *          false if the noteToCheck was not played
         *          null if there was no dominant note when listening
         */
        fun isNotePlayedCorrectly(noteToCheck : ScoreElement,
                                  dominancePercentage : Double = 0.95): Boolean? {

            val listOfNotes = mutableListOf<String>()
            val start = System.currentTimeMillis()
            val durationMs = noteToCheck.getDuration()

            // Listening for the amount of time said in samplingInterval
            while (System.currentTimeMillis() - start < durationMs) {
                val note = PitchDetector.getLastDetectedNote()
                listOfNotes.add(note)
                //println("Escuchando nota: $note")
                Thread.sleep(durationMs)
            }

            // Get the frequency of each note
            val noteCounts = listOfNotes.groupingBy { it }.eachCount()
            val totalSamples = listOfNotes.size
            val threshold = (totalSamples * dominancePercentage).toInt()

            // find the note that appears at least 95%
            val dominantNote = noteCounts.entries.find { it.value >= threshold }?.key
            var name ="Z"
            if(noteToCheck is Note){
                name= noteToCheck.getName().toString()+noteToCheck.getOctave().toString()
            }

            Log.d("SHEETcHECKER", "Esperado: $name, Detectado: $dominantNote")

            return dominantNote?.let { noteToCheck.check(NoteDominant(notaBase(it),octave(it)))  }
        }*/

    fun notaBase(nota: String): Char {
        return nota[0]
    }
    fun octave(nota:String):Int{
        return Regex("\\d+").find(nota)?.value?.toInt()!!
    }


}