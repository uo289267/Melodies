package tfg.uniovi.melodies.tools.pitchdetector



import android.util.Log
import tfg.uniovi.melodies.entities.notes.Note
import tfg.uniovi.melodies.entities.notes.NoteDominant
import tfg.uniovi.melodies.entities.notes.interfaces.ScoreElement

class SheetChecker2() {
    val notes = arrayOf("C", "D", "E", "F", "G", "A", "B")

    // Variables para detección de onset
    private var lastDetectedNote: String? = null
    private var lastNoteChangeTime: Long = 0
    private var lastSilenceTime: Long = 0
    private var wasInSilence: Boolean = false
    private val minimumNoteStabilityMs = 100L // Tiempo mínimo para considerar una nota estable
    private val minimumSilenceMs = 50L // Tiempo mínimo de silencio para detectar nueva nota repetida

    /**
     * Detecta si una nueva nota ha comenzado a sonar (incluyendo notas repetidas)
     * @return la nota detectada si es un onset, null si no hay cambio
     */
    private fun detectNoteOnset(): String? {
        val currentNote = PitchDetector.getLastDetectedNote()
        val currentTime = System.currentTimeMillis()

        // Si no hay nota detectada (silencio)
        if (currentNote.isEmpty() || currentNote == "Desconocido") {
            if (!wasInSilence) {
                wasInSilence = true
                lastSilenceTime = currentTime
                Log.d("ONSET_DETECTOR", "Silencio detectado")
            }
            return null
        }

        // Si había silencio y ahora hay una nota
        if (wasInSilence) {
            if (currentTime - lastSilenceTime > minimumSilenceMs) {
                wasInSilence = false
                lastDetectedNote = currentNote
                lastNoteChangeTime = currentTime
                Log.d("ONSET_DETECTOR", "Nueva nota después de silencio: $currentNote")
                return currentNote
            }
        }
        // Si cambió la nota (diferente a la anterior)
        else if (lastDetectedNote != currentNote) {
            if (currentTime - lastNoteChangeTime > minimumNoteStabilityMs) {
                lastDetectedNote = currentNote
                lastNoteChangeTime = currentTime
                Log.d("ONSET_DETECTOR", "Cambio de nota detectado: $currentNote")
                return currentNote
            }
        }

        return null
    }

    /**
     * Resetea el detector de onset para prepararlo para la siguiente nota
     */
    private fun resetOnsetDetector() {
        Log.d("ONSET_DETECTOR", "Reseteando detector para próxima nota")
        lastDetectedNote = null
        lastNoteChangeTime = 0
        lastSilenceTime = 0
        wasInSilence = false
    }

    /**
     * MÉTODO ALTERNATIVO: Detección de onset basada en cambios de amplitud
     * Útil para detectar notas repetidas cuando no hay silencio entre ellas
     */
    private fun detectOnsetByAmplitude(): String? {
        // Este método necesitaría acceso a la amplitud del PitchDetector
        // Por ahora, usamos un enfoque temporal
        val currentNote = PitchDetector.getLastDetectedNote()

        if (currentNote.isEmpty() || currentNote == "Desconocido") {
            return null
        }

        // Si hace más de X tiempo que no se "confirma" una nueva nota,
        // consideramos que puede ser una repetición
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNoteChangeTime > 500L) { // 0.5 segundo sin cambios
            Log.d("ONSET_DETECTOR", "Posible nota repetida detectada por tiempo: $currentNote")
            lastNoteChangeTime = currentTime
            return currentNote
        }

        return null
    }

    /**
     * Espera a detectar el inicio de la nota esperada (con soporte para notas repetidas)
     * @param noteToCheck La nota que esperamos
     * @param timeoutMs Tiempo máximo de espera
     * @param allowSameNote Si true, permite detectar la misma nota que se estaba tocando
     * @return true si detecta la nota esperada, false si detecta otra, null si timeout
     */
    private fun waitForExpectedNoteOnset(noteToCheck: ScoreElement,
                                         timeoutMs: Long = 10000L,
                                         allowSameNote: Boolean = false): Boolean? {
        val startTime = System.currentTimeMillis()

        var expectedNoteName = "Z"
        if (noteToCheck is Note) {
            expectedNoteName = noteToCheck.name.toString() + noteToCheck.octave.toString()
        }

        Log.d("SHEET_CHECKER", "Esperando inicio de nota: $expectedNoteName (permitir repetida: $allowSameNote)")

        // Si permitimos la misma nota y ya está sonando la correcta, continuar inmediatamente
        if (allowSameNote) {
            val currentNote = PitchDetector.getLastDetectedNote()
            if (currentNote.isNotEmpty() && currentNote != "Desconocido") {
                val isCurrentCorrect = noteToCheck.check(NoteDominant(notaBase(currentNote), octave(currentNote)))
                if (isCurrentCorrect) {
                    Log.d("SHEET_CHECKER", "Nota esperada ya está sonando: $currentNote")
                    return true
                }
            }
        }

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val detectedOnset = detectNoteOnset()

            // Si no se detecta onset normal, intentar por amplitud/tiempo
            val finalOnset = detectedOnset ?: detectOnsetByAmplitude()

            finalOnset?.let { detectedNote ->
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
     * Maneja correctamente notas repetidas
     * @param noteToCheck La nota que esperamos
     * @param dominancePercentage Porcentaje mínimo de dominancia (por defecto 0.95)
     * @param onsetTimeoutMs Tiempo máximo para esperar el onset (por defecto 10 segundos)
     * @param isRepeatedNote Indica si esta nota es igual a la anterior
     * @return true si nota correcta, false si incorrecta, null si no se detectó onset
     */
    fun isNotePlayedCorrectlyWithOnset(noteToCheck: ScoreElement,
                                       dominancePercentage: Double = 0.95,
                                       onsetTimeoutMs: Long = 10000L,
                                       isRepeatedNote: Boolean = false): Boolean? {



        Log.d("SHEET_CHECKER", "=== INICIANDO VERIFICACIÓN DE NOTA: $noteToCheck (repetida: $isRepeatedNote) ===")

        // Si es una nota repetida, resetear el detector
        if (isRepeatedNote) {
            resetOnsetDetector()
            // Esperar un poco para detectar el nuevo onset de la nota repetida
            Thread.sleep(50)
        }

        // PASO 1: Esperar a que comience la nota esperada
        val onsetDetected = waitForExpectedNoteOnset(noteToCheck, onsetTimeoutMs, isRepeatedNote)


        /*when (onsetDetected) {
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
        }*/

        // PASO 2: Una vez detectado el onset correcto, verificar durante la duración
        if(onsetDetected!= null){
            if(!onsetDetected)
                return false
            else{
                val listOfNotes = mutableListOf<String>()
                val start = System.currentTimeMillis()
                val durationMs = noteToCheck.getDuration()

                Log.d("SHEET_CHECKER", "Verificando nota durante ${durationMs}ms...")


                while (System.currentTimeMillis() - start < durationMs) {
                    val note = PitchDetector.getLastDetectedNote()
                    listOfNotes.add(note)
                    Thread.sleep(50)
                }

                // PASO 3: Calcular dominancia como en tu método original
                val noteCounts = listOfNotes.groupingBy { it }.eachCount()
                val totalSamples = listOfNotes.size
                val threshold = (totalSamples * dominancePercentage).toInt()

                // Encontrar la nota que aparece al menos el porcentaje requerido
                val dominantNote = noteCounts.entries.find { it.value >= threshold }?.key

                Log.d("SHEET_CHECKER", "Muestras totales: $totalSamples, Umbral: $threshold")
                Log.d("SHEET_CHECKER", "Conteos de notas: $noteCounts")
                Log.d("SHEET_CHECKER", "Nota dominante: $dominantNote, Esperada: $noteToCheck")

                val result = dominantNote?.let {
                    noteToCheck.check(NoteDominant(notaBase(it), octave(it)))
                }

                Log.d("SHEET_CHECKER", "=== RESULTADO FINAL: ${result ?: "null"} ===")

                return result
            }
        }
        return false
    }

    /**
     * Método original mantenido para compatibilidad
     */
    fun isNotePlayedCorrectly(noteToCheck: ScoreElement,
                              dominancePercentage: Double = 0.95): Boolean? {

        val listOfNotes = mutableListOf<String>()
        val start = System.currentTimeMillis()
        val durationMs = noteToCheck.getDuration()

        // Listening for the amount of time said in samplingInterval
        while (System.currentTimeMillis() - start < durationMs) {
            val note = PitchDetector.getLastDetectedNote()
            listOfNotes.add(note)
            Thread.sleep(50) // Reducido el sleep para mejor responsividad
        }

        // Get the frequency of each note
        val noteCounts = listOfNotes.groupingBy { it }.eachCount()
        val totalSamples = listOfNotes.size
        val threshold = (totalSamples * dominancePercentage).toInt()

        // find the note that appears at least 95%
        val dominantNote = noteCounts.entries.find { it.value >= threshold }?.key
        var name = "Z"
        if (noteToCheck is Note) {
            name = noteToCheck.name.toString() + noteToCheck.octave.toString()
        }

        Log.d("SHEET_CHECKER", "Esperado: $name, Detectado: $dominantNote")

        return dominantNote?.let { noteToCheck.check(NoteDominant(notaBase(it), octave(it))) }
    }

    fun notaBase(nota: String): Char {
        return nota[0]
    }

    fun octave(nota: String): Int {
        return Regex("\\d+").find(nota)?.value?.toInt() ?: 4 // Default octave 4 si no se encuentra
    }
}