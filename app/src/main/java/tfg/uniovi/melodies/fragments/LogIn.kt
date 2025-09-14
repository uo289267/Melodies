package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentLogInBinding
import tfg.uniovi.melodies.fragments.viewmodels.LogInViewModel
import tfg.uniovi.melodies.fragments.viewmodels.LogInViewModelProviderFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.utils.TextWatcherAdapter
/**
 * Fragment responsible for handling user login.
 * Allows users to enter an existing user ID or create a new user.
 * Observes ViewModel for login status and navigates accordingly.
 * Also manages visibility of the bottom navigation menu during login.
 */
class LogIn : Fragment() {
    private lateinit var  binding: FragmentLogInBinding
    private lateinit var logInViewModel: LogInViewModel
    private val userIdInputWatcher = object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            //communicating to viewmodel
            this@LogIn.logInViewModel.updateUserId(s.toString())
        }
    }

    override fun onStop() {
        super.onStop()
        //Pitch detector stop listening and processing audio
        setBottomNavMenuVisibility(View.VISIBLE)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        binding.inputUserId.addTextChangedListener(userIdInputWatcher)
        logInViewModel = ViewModelProvider(this, LogInViewModelProviderFactory())[LogInViewModel::class.java]
        logInViewModel.userId.observe(viewLifecycleOwner){ userId ->
            modifyUserIdEditText(binding.inputUserId, userId)
        }
        binding.btnEnterPreviousAccount.setOnClickListener {
            val currentId = binding.inputUserId.text.toString().trim()
            if (currentId.isNotEmpty()) {
                logInViewModel.updateUserId(currentId)
                logInViewModel.checkIfUserExists()
            } else {
                binding.layoutUserId.error = getString(R.string.error_user_doesnt_exist)
            }
        }
        logInViewModel.userExists.observe(viewLifecycleOwner){ exists ->
            if(exists){
                Log.e("LOGIN", "User exists")
                Toast.makeText(requireContext(), getString(R.string.log_in_successfull), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_logIn_to_home_fragment)
                PreferenceManager.saveUserId(requireContext(), logInViewModel.userId.value!!)
            }
        }
        binding.btnEnterAsNewUser.setOnClickListener{
            logInViewModel.createAndLoadNewUser(requireContext())
        }
        logInViewModel.newUserId.observe(viewLifecycleOwner){
            if(!logInViewModel.newUserId.value.isNullOrEmpty())
                findNavController().navigate(R.id.action_logIn_to_home_fragment)
        }
        return binding.root
    }

        override fun onResume() {
        super.onResume()
        setBottomNavMenuVisibility(View.GONE)
    }


    /**
     * Changes the visibility of the Bottom Navigation
     *
     * @param visibility the value for the Bottom Navigation Menu Visibility (View.VISIBLE/View.GONE)
     */
    private fun setBottomNavMenuVisibility(visibility: Int){
        if(visibility == View.GONE || visibility == View.VISIBLE ||visibility == View.INVISIBLE){
            val navView =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            navView.visibility = visibility
        }
    }
    /**
     * Updates the user ID EditText field with a new value without triggering the text watcher.
     *
     * @param etName The EditText view for the user ID input.
     * @param newName The new user ID string to set in the EditText.
     */
    private fun modifyUserIdEditText(etName: EditText, newName:String){
        etName.apply {
            removeTextChangedListener(userIdInputWatcher)
            val currentSelection = selectionStart
            setText(newName)
            setSelection(currentSelection, newName.length)
            addTextChangedListener(userIdInputWatcher)
        }
    }
}