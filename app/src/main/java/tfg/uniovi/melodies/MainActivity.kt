package tfg.uniovi.melodies

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

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

}