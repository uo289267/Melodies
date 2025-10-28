package tfg.uniovi.melodies.application

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.util.Log
import tfg.uniovi.melodies.R
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MelodiesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.unexpected_error),
                    Toast.LENGTH_LONG
                ).show()
            }

            // Save log
            saveExceptionToFile(exception)
        }
    }

    private fun saveExceptionToFile(exception: Throwable) {
        try {
            // create melodies/log folder
            val logDir = File(filesDir, "melodies/log")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "crash_${sdf.format(Date())}.log"
            val logFile = File(logDir, fileName)

            // Write stacktrace
            FileWriter(logFile, true).use { fw ->
                PrintWriter(fw).use { pw ->
                    pw.println("Thread: ${Thread.currentThread().name}")
                    exception.printStackTrace(pw)
                }
            }

            Log.d("MelodiesApp", "Crash log guardado en: ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("MelodiesApp", "Error al guardar log: ${e.message}")
        }
    }
}
