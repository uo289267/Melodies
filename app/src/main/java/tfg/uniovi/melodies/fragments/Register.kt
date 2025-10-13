package tfg.uniovi.melodies.fragments

import android.os.Bundle
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
import tfg.uniovi.melodies.databinding.FragmentRegisterBinding
import tfg.uniovi.melodies.fragments.viewmodels.RegisterViewModel
import tfg.uniovi.melodies.fragments.viewmodels.RegisterViewModelProviderFactory
import tfg.uniovi.melodies.utils.TextWatcherAdapter

/**
 * A simple [Fragment] subclass.
 * Use the factory method to create an instance of this fragment.
 */
class Register : Fragment() {

    private lateinit var  binding: FragmentRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel
    private val userIdInputWatcher = object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            //communicating to viewmodel
            this@Register.registerViewModel.updateNickname(s.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater,container,false)
        binding.inputNickname.addTextChangedListener(userIdInputWatcher)
        registerViewModel = ViewModelProvider(this, RegisterViewModelProviderFactory())[RegisterViewModel::class.java]
        registerViewModel.nickname.observe(viewLifecycleOwner){ newName ->
            modifyNicknameEditText(binding.inputNickname, newName)
        }
        binding.btnRegister.setOnClickListener{
            val currentNickname = binding.inputNickname.text.toString().trim()
            if(currentNickname.isEmpty())
                binding.layoutUserId.error = getString(R.string.login_wrong_blank_err)
            else
                registerViewModel.checkIfUserExists()
        }
        registerViewModel.userExists.observe(viewLifecycleOwner){ exists->
            if(exists)
                binding.layoutUserId.error = getString(R.string.nickname_unable)
            else
                if(!registerViewModel.nickname.value.isNullOrEmpty())
                    registerViewModel.createAndLoadNewUser(
                        binding.inputNickname.text.toString(), requireContext())
        }
        registerViewModel.newUserId.observe(viewLifecycleOwner){ id ->
            if(!id.isNullOrEmpty()){
                findNavController().navigate(R.id.action_register_to_home_fragment)
                Toast.makeText(context, getString(R.string.log_in_successfull), Toast.LENGTH_SHORT)
                    .show()

            }

        }
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        setBottomNavMenuVisibility(View.GONE)
    }
    override fun onStop() {
        super.onStop()
        //Pitch detector stop listening and processing audio
        setBottomNavMenuVisibility(View.VISIBLE)
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
    private fun modifyNicknameEditText(etName: EditText, newName:String){
        etName.apply {
            removeTextChangedListener(userIdInputWatcher)
            val currentSelection = selectionStart
            setText(newName)
            setSelection(currentSelection, newName.length)
            addTextChangedListener(userIdInputWatcher)
        }
    }
}