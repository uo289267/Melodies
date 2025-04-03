package tfg.uniovi.melodies.tools.pitchdetector

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import kotlin.math.ln

object PitchDetector {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048
    private var dispatcher: AudioDispatcher? = null
    private var audioThread: Thread? = null
    @Volatile private var isRunning = false
    private var lastDetectedNote: String = "None"

    fun startListening() {

        if (isRunning) return  // Evitar múltiples inicios
        isRunning = true

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, 0)

        val pdh = PitchDetectionHandler { result, _ ->
            if (result.pitch != -1f) {
                val pitchInHz = result.pitch
                lastDetectedNote = convertFrequencyToNote(pitchInHz)
                Log.d("PITCH","Frecuencia: $pitchInHz Hz, Nota: $lastDetectedNote")
            }
        }

        dispatcher?.addAudioProcessor(
            PitchProcessor(PitchEstimationAlgorithm.YIN, SAMPLE_RATE.toFloat(), BUFFER_SIZE, pdh)
        )

        audioThread = Thread {
            Log.d("PITCH", "Iniciando detección...")
            dispatcher?.run()
            Log.d("PITCH", "Dispatcher detenido")
            isRunning = false // Restablecer cuando termine
        }.apply { start() }
    }

    fun stopListening() {
        if (!isRunning) return  // Si no está corriendo, salir

        isRunning = false
        dispatcher?.stop() // Detener el procesamiento de audio
        audioThread?.join() // Esperar a que termine
    }

    fun getLastDetectedNote(): String = lastDetectedNote

    private fun convertFrequencyToNote(frequency: Float): String {
        // Notas musicales en sistema Do, Re, Mi
        val solfegeNotes =
            arrayOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
        // Referencia A4 = 440 Hz
        val A4 = 440
        val noteIndex = Math.round(12 * ln((frequency / A4).toDouble()) / ln(2.0)).toInt()
        val noteNumber = (noteIndex + 69) % 12
        val octave = (noteIndex + 69) / 12

        return if (noteNumber >= 0 && noteNumber < solfegeNotes.size) {
            solfegeNotes[noteNumber] + octave
        } else {
            "Desconocido"
        }
    }
}