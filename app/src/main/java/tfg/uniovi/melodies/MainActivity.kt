package tfg.uniovi.melodies

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.processing.PitchDetector.MIC_REQ_CODE
import tfg.uniovi.melodies.processing.PitchDetector.startListening

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavView : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        bottomNavView = findViewById(R.id.bottomNavigationView)
        navController = findNavController(R.id.fragmentContainerView)

        if (savedInstanceState == null) {
            checkUserAndNavigate()
        }
        setupBottomNavigation()
        controlBottomNavigationVisibility()

    }
    private fun checkUserAndNavigate() {
        val userId = PreferenceManager.getUserId(this)
        if (userId != null) {
            if (userId.isNotEmpty()) {
                // User had logged in -> go to home
                navController.navigate(R.id.home_fragment)
            } else {
                // User has not logged in -> go to login
                navController.navigate(R.id.logIn)
            }
        }
    }

    private fun setupBottomNavigation() {
        //bottomNavView.setupWithNavController(navController)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_fragment -> {
                    navController.popBackStack(R.id.home_fragment, false)
                    if (navController.currentDestination?.id != R.id.home_fragment) {
                        navController.navigate(R.id.home_fragment)
                    }
                    true
                }
                R.id.importing -> {
                    navController.popBackStack(R.id.importing, false)
                    if (navController.currentDestination?.id != R.id.importing) {
                        navController.navigate(R.id.importing)
                    }
                    true
                }
                R.id.fullLibrary -> {
                    navController.popBackStack(R.id.fullLibrary, false)
                    if (navController.currentDestination?.id != R.id.fullLibrary) {
                        navController.navigate(R.id.fullLibrary)
                    }
                    true
                }
                R.id.profile -> {
                    navController.popBackStack(R.id.profile, false)
                    if (navController.currentDestination?.id != R.id.profile) {
                        navController.navigate(R.id.profile)
                    }
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home_fragment -> bottomNavView.selectedItemId = R.id.home_fragment
                R.id.importing -> bottomNavView.selectedItemId = R.id.importing
                R.id.profile -> bottomNavView.selectedItemId = R.id.profile
            }
        }
    }

    private fun controlBottomNavigationVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavView.visibility = when (destination.id) {
                R.id.logIn -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    // Callback response to microphon permissions
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

