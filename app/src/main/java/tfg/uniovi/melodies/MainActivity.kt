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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.repositories.FolderFirestore
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector

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

        val folderFirestore = FolderFirestore()
        lifecycleScope.launch {
            /*
            val musicXMLSheetAll :List<MusicXMLSheet> =
                folderFirestore.getAllSheetsFromFolder("7f7bbeb3-010b-4418-8453-bc618d8fbbb9")
            musicXMLSheetAll.forEach {
                Log.d("FIRESTORE",it.name);
            }*/
            val folders = folderFirestore.getAllFolders();
            Log.d("FIRESTORE", folders.toString())
        }


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