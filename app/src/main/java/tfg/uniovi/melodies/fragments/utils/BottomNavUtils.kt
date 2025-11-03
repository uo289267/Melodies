package tfg.uniovi.melodies.fragments.utils


import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import tfg.uniovi.melodies.R

object BottomNavUtils {

    /**
     * Changes the visibility of the Bottom Navigation
     *
     * @param fragment the Fragment from which to access the activity
     * @param visibility the value for the Bottom Navigation Menu Visibility (View.VISIBLE/View.GONE)
     */
    fun setBottomNavMenuVisibility(fragment: Fragment, visibility: Int) {
        if (visibility == View.GONE || visibility == View.VISIBLE) {
            val navView = fragment.activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView?.visibility = visibility
        }
    }
}
