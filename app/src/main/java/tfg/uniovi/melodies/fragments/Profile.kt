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
import tfg.uniovi.melodies.utils.ShowAlertDialog.showInputNewNicknameDialog

private const val LOGOUT_TAG = "LOGOUT"
private const val AVATAR_HTTPS = "https://anonymous-animals.azurewebsites.net/avatar/"
private const val RENAME = "RENAME"

class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelProviderFactory()
        )[ProfileViewModel::class.java]

        val userId = PreferenceManager.getUserId(requireContext())!!

        profileViewModel.loadNickname(userId)
        profileViewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.tvNickname.text = nickname
        }

        binding.btnEdit.setOnClickListener {
            showInputNewNicknameDialog(
                context = requireContext(),
                lifecycleOwner = viewLifecycleOwner,
                profileViewModel = profileViewModel,
                titleRes = ContextCompat.getString(requireContext(), R.string.nickname_rename),
                messageRes = ContextCompat.getString(requireContext(), R.string.nickname_new_name)
                        +" "+profileViewModel.nickname.value+" "+
                        ContextCompat.getString(requireContext(), R.string.nickname_new_name2),
                validations = listOf(
                    {newNick:String -> newNick.isNotEmpty() } to getString(R.string.rename_nick_empty_err),
                    {newNick:String -> newNick.length <= 20 } to getString(R.string.rename_nick_length_err),
                ),
                onConfirm = { nickname ->
                    Log.d(RENAME, "New nick: $nickname")
                    profileViewModel.renameUserNewNickname(userId, nickname)
                }
            )
        }

        val apiUrl = "$AVATAR_HTTPS$userId"
        binding.ivProfileAvatar.load(apiUrl)

        binding.btnLogOut.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.log_out_quest)
                .setMessage(getString(R.string.log_out_question))
                .setIcon(R.drawable.icon_alert)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    doLogout(userId)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    Log.d(LOGOUT_TAG, "$userId did not log out")
                }
                .show()
        }

        return binding.root
    }

    private fun doLogout(userId: String) {
        Log.d(LOGOUT_TAG, "$userId logged out")
        PreferenceManager.clearUserId(requireContext())
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        findNavController().navigate(R.id.action_profile_to_logIn)
    }
}
