package tfg.uniovi.melodies.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil3.load
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentProfileBinding
import tfg.uniovi.melodies.fragments.viewmodels.ProfileViewModel
import tfg.uniovi.melodies.fragments.viewmodels.ProfileViewModelProviderFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.utils.ShowAlertDialog

private const val LOGOUT_TAG = "LOGOUT"
private const val PROFILE_TAG = "PROFILE"

private const val AVATAR_HTTPS = "https://anonymous-animals.azurewebsites.net/avatar/"

private const val RENAME = "RENAME"

/**
 * Fragment responsible for displaying the user profile.
 * Shows the current user ID, allows copying the ID to the clipboard,
 * displays the user's avatar, and provides a logout functionality with confirmation dialog.
 */
class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        profileViewModel = ViewModelProvider(this, ProfileViewModelProviderFactory())[ProfileViewModel::class.java]
        val userId = PreferenceManager.getUserId(requireContext())!!
        profileViewModel.loadNickname(userId)
        profileViewModel.nickname.observe(viewLifecycleOwner){ nickname ->
            binding.tvNickname.text = nickname
        }

        binding.btnEdit.setOnClickListener {
            view?.let { view ->
                ShowAlertDialog.showInputDialog(
                    context = view.context,
                    titleRes = ContextCompat.getString(view.context, R.string.nickname_rename),
                    messageRes = ContextCompat.getString(view.context, R.string.nickname_new_name),
                    validations = listOf(
                        Pair({ it.isNotEmpty() },
                            ContextCompat.getString(view.context, R.string.rename_nick_empty_err)
                        ),
                        Pair({ it.length <= 20 },
                            ContextCompat.getString(view.context, R.string.rename_nick_length_err)
                        ),
                        ({ it : String->
                            profileViewModel.checkNicknameAvailability()
                            val exists = profileViewModel.isNicknameTaken.value ?: false
                            exists
                        } to getString(R.string.nickname_unable))
                    )

                ) { newName ->
                    Log.d(RENAME, "Renaming nickname to: $newName")
                    profileViewModel.renameUserNewNickname(userId, newName)
                    profileViewModel.loadNickname(userId)
                }
            }
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
}