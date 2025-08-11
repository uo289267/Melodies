package tfg.uniovi.melodies.tools.pitchdetector



import android.util.Log
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

        Log.d("SHEET_CHECKER", "Esperando inicio de nota: $noteToCheck (permitir repetida: $allowSameNote)")

        // Si permitimos la misma nota y ya está sonando la correcta, continuar inmediatamente
        if (allowSameNote) {
            val currentNote = PitchDetector.getLastDetectedNote()
            if (currentNote.isNotEmpty() && currentNote != "Desconocido") {
                // TODO val isCurrentCorrect = noteToCheck.peek(NoteDominant(baseNote(currentNote), octave(currentNote)))
                val isCurrentCorrect = noteToCheck.check(NoteDominant(baseNote(currentNote), octave(currentNote)))
                if (isCurrentCorrect) {
                    Log.d("SHEET_CHECKER", "Nota esperada ya está sonando: $currentNote")
                    return true
                }
            }
        }

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val detectedOnset = PitchDetector.getLastDetectedNote()

            // Si no se detecta onset normal, intentar por amplitud/tiempo
            val finalOnset = detectedOnset ?: detectOnsetByAmplitude()

            finalOnset?.let { detectedNote ->
                Log.d("SHEET_CHECKER", "Onset detectado: $detectedNote, Esperado: $noteToCheck")

                // Verificar si la nota detectada coincide con la esperada
                //TODO val isCorrect = noteToCheck.peek(NoteDominant(baseNote(detectedNote), octave(detectedNote)))
                val isCorrect = noteToCheck.check(NoteDominant(baseNote(detectedNote), octave(detectedNote)))
                return isCorrect
            }

            // Pequeña pausa para no sobrecargar el procesador
            Thread.sleep(50)
        }

        Log.d("SHEET_CHECKER", "Timeout esperando nota: $noteToCheck")
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
                                       dominancePercentage: Double = 0.8,
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

        // PASO 2: Una vez detectado el onset correcto, verificar durante la duración
        if(onsetDetected!= null){
            if(!onsetDetected)
                return false
            else{
                val listOfNotes = mutableListOf<String>()
                val start = System.currentTimeMillis()
                val durationMs = noteToCheck.getDuration()*0.95

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

                val result : Boolean?
                if(dominantNote!=null)
                    result = noteToCheck.check(NoteDominant(baseNote(dominantNote), octave(dominantNote)))
                else
                    result = noteToCheck.check(null)

                Log.d("SHEET_CHECKER", "=== RESULTADO FINAL: $result ===")

                return result
            }
        }
        return false
    }

    private fun baseNote(nota: String): Char {
        return nota[0]
    }

    fun octave(nota: String): Int {
        return Regex("\\d+").find(nota)?.value?.toInt() ?: 5
    }
}