package tfg.uniovi.melodies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.MIC_REQ_CODE
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.startListening

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = findNavController(R.id.fragmentContainerView)
        bottomNavView.setupWithNavController(navHostFragment)

        val userId = PreferenceManager.getUserId(this)
        if (userId != null) {
            // ya hay usuario guardado, navegar a pantalla principal
            navHostFragment.navigate(R.id.home_fragment)
        } else {
            // mostrar pantalla de login
            navHostFragment.navigate(R.id.logIn)
        }
        //requestMicPermission()
/*
        var prev = PitchDetector.getLastDetectedNote()
        while(true) {
            if (prev != PitchDetector.getLastDetectedNote()) {

                prev = PitchDetector.getLastDetectedNote()
                Log.d("MINE", "last Note:${prev}")
            }
        }*/


        /*
        val RECORD_AUDIO_PERMISSION_CODE = 101
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),RECORD_AUDIO_PERMISSION_CODE);
            //TODO it will not activate the audio after asking the permission
        }else{
            PitchDetector.startListening()
        }

        var i = 1000000000
        var prev = PitchDetector.getLastDetectedNote()
        while(true){
            if(prev!=PitchDetector.getLastDetectedNote()){

                prev =  PitchDetector.getLastDetectedNote()
                Log.d("MINE", "last Note:${prev}")
            }
            //tv.text= detector.lastNote
            i--
        }
        PitchDetector.stopListening()*/


    }
    // Callback cuando el usuario responde a la solicitud de permisos

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MIC_REQ_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("PITCH_DETECTOR", "Permission accepted")
            //pitch detector start listening
            startListening(lifecycleScope)
        } else {
            Log.d("PITCH_DETECTOR", "Permission denied")
        }
    }




}

