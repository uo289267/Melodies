package tfg.uniovi.melodies.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil3.load
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentProfileBinding
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.utils.ShowAlertDialog

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val userId = PreferenceManager.getUserId(requireContext())!!
        binding.tvUserId.text = userId
        binding.btnCopy.setOnClickListener {
            copyToClipBoard("PROFILE", userId,
                "User Id copied in clipboard")
        }
        val apiUrl= "https://anonymous-animals.azurewebsites.net/avatar/$userId"
        binding.ivProfileAvatar.load(apiUrl)
        binding.btnLogOut.setOnClickListener{
            AlertDialog.Builder(requireContext()).setTitle("Log out?")
                .setMessage(
                    "Are you sure you want to log out?"
                )
                .setIcon(R.drawable.icon_alert)
                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                    doLogout(userId)
                }.setNegativeButton(android.R.string.cancel){ dialogInterface, i ->
                    Log.d("LOGOUT", "$userId did not log out")
                }.show()
        }
        return binding.root
    }

    private fun doLogout(userId: String) {
        Log.d("LOGOUT", "$userId logged out")
        PreferenceManager.clearUserId(requireContext())
        // Reinicia la actividad
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun copyToClipBoard(label:String, content: String, toastMsg: String){
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
    }


}