package tfg.uniovi.melodies.tools.pitchdetector

import android.util.Log
import kotlin.random.Random

class SheetChecker () {
    val solfegeNotes =
        arrayOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Si")
    var noteToPlay ="Do"
    var numTimes = 0
    fun getNotesToPlay(): String {
        var num = Random.nextInt(solfegeNotes.size)
        noteToPlay = solfegeNotes[num]
        return solfegeNotes[num]
    }
    fun areNotesPlayedCorrectly(): Boolean{/*
        var lastNote = PitchDetector.getLastDetectedNote().substring(0,2)
        while(lastNote!=noteToPlay){
                lastNote = PitchDetector.getLastDetectedNote().substring(0,2)
            Log.d("sound", "We wanted $noteToPlay but got $lastNote")
        }
        return true*/
/*
        var numTimes = 3
        var lastNote = PitchDetector.getLastDetectedNote()
        while(numTimes!=0){
            if(!lastNote.contains(noteToPlay) ){
                lastNote = PitchDetector.getLastDetectedNote()
                Log.d("sound", "We wanted $noteToPlay but got $lastNote")
            }

            else{
                Log.d("SHEETcHECKER", "NOTE: $noteToPlay HAS BEEN PLAYED")
                numTimes--
            }

        }*/

        var listOfNotes : MutableList<String> = mutableListOf()
        var start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 1000) {
            // Ejecuta esto durante 1 segundo
            listOfNotes.add(PitchDetector.getLastDetectedNote())
            println("Ejecutando...")
            Thread.sleep(100) // Evita que sature el CPU
        }
        var freq = notaMasFrecuente(listOfNotes)
        if (freq.equals(noteToPlay)){
            numTimes++
            return true
        }
        else
            return false


    }

    fun notaBase(nota: String): String {
        return when {
            nota.length >= 4 && nota[3] == '#' -> nota.substring(0, 4) // ej: "Sol#7" → "Sol#"
            nota.length >= 3 && nota.substring(0, 3) in listOf("Sol", "La#", "Si#", "Do#", "Re#", "Fa#", "Mi#") -> nota.substring(0, 3)
            else -> nota.substring(0, 2) // ej: "Si6", "La5"
        }
    }

    fun notaMasFrecuente(notas: List<String>): String? {
        val frecuencia = notas
            .map { notaBase(it) }
            .groupingBy { it }
            .eachCount()

        return frecuencia.maxByOrNull { it.value }?.key
    }



}