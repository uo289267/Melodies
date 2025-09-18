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

private const val LOGOUT_TAG = "LOGOUT"
private const val PROFILE_TAG = "PROFILE"

private const val AVATAR_HTTPS = "https://anonymous-animals.azurewebsites.net/avatar/"

/**
 * Fragment responsible for displaying the user profile.
 * Shows the current user ID, allows copying the ID to the clipboard,
 * displays the user's avatar, and provides a logout functionality with confirmation dialog.
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
            copyToClipBoard(
                PROFILE_TAG, userId,
                getString(R.string.text_copied_in_clipboard))
        }
        val apiUrl= "$AVATAR_HTTPS$userId"
        binding.ivProfileAvatar.load(apiUrl)
        binding.btnLogOut.setOnClickListener{
            AlertDialog.Builder(requireContext()).setTitle(R.string.log_out_quest)
                .setMessage(
                    getString(R.string.log_out_question)
                )
                .setIcon(R.drawable.icon_alert)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    doLogout(userId)
                }.setNegativeButton(android.R.string.cancel){ _, _ ->
                    Log.d(LOGOUT_TAG, "$userId did not log out")
                }.show()
        }
        return binding.root
    }
    /**
     * Performs the logout process by clearing the stored user ID
     * and restarting the MainActivity to reset the app state.
     *
     * @param userId The ID of the user who is logging out.
     */
    private fun doLogout(userId: String) {
        Log.d(LOGOUT_TAG, "$userId logged out")
        PreferenceManager.clearUserId(requireContext())
        // Restarts the MainActivity
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        findNavController().navigate(R.id.action_profile_to_logIn)
    }
    /**
     * Copies a given string to the clipboard and shows a Toast message.
     *
     * @param label A label describing the content being copied.
     * @param content The string content to copy to the clipboard.
     * @param toastMsg The message to show in the Toast after copying.
     */
    private fun copyToClipBoard(label:String, content: String, toastMsg: String){
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
    }


}