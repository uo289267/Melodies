package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentAddFolderBinding
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.fragments.adapters.SpinnerFoldersColorsAdapter
import tfg.uniovi.melodies.fragments.viewmodels.AddFolderViewModel
import tfg.uniovi.melodies.fragments.viewmodels.AddFolderViewModelProviderFactory
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModel
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModelProviderFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.utils.TextWatcherAdapter

private const val CREATE_FOLDER_TAG = "CREATE_FOLDER"

private const val MAX_FOLDER_NAME_LENGTH = 30

/**
 * Fragment responsible for creating a new folder.
 * Allows entering a folder name, choosing a color, validating inputs,
 * and interacting with the ViewModels to save the folder.
 */
class AddFolder : Fragment() {

    private lateinit var  binding: FragmentAddFolderBinding
    private lateinit var foldersViewModel: FolderViewModel
    private lateinit var addFolderViewModel: AddFolderViewModel

    private val folderNameWatcher = object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            //communicating to viewmodel
            this@AddFolder.addFolderViewModel.updateFolderName(s.toString())
        }
    }
    private val colors = listOf(
        Triple(R.drawable.folder_yellow,R.string.yellow, Colors.YELLOW),
        Triple(R.drawable.folder_pink,R.string.pink, Colors.PINK),
        Triple(R.drawable.folder_blue, R.string.blue, Colors.BLUE)
    )
    private var colorSelected: Triple<Int,Int,Colors> = colors[0]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //FolderViewModelProvider
        val owner = findNavController().getViewModelStoreOwner(R.id.navigation)
        val factory = FolderViewModelProviderFactory(PreferenceManager.getUserId(requireContext())!!)
        foldersViewModel = ViewModelProvider(owner, factory)[FolderViewModel::class.java]


        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFolderBinding.inflate(inflater, container, false)

        configureSpinner(binding.spFolderColors)
        configureButtonAddFolder(binding)
        binding.folderNameInput.addTextChangedListener(folderNameWatcher)

        addFolderViewModel = ViewModelProvider(this, AddFolderViewModelProviderFactory(
            PreferenceManager.getUserId(requireContext())!!
        ))[AddFolderViewModel::class.java]

        addFolderViewModel.folderDTO.observe(viewLifecycleOwner){dto ->
            modifyFolderName(binding.folderNameInput, dto.name)
        }

        addFolderViewModel.folderNameExists.observe(viewLifecycleOwner){
            exists ->
            if(!exists){
                Log.e(CREATE_FOLDER_TAG, "Folder was created")
                addFolderViewModel.createFolder()
                findNavController().popBackStack()
            }else{
                binding.folderName.setError(getString(R.string.error_folder_name_exists_already))
            }
        }
        binding.toolbar.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return binding.root
    }
    /**
     * Updates the text in the folder name input field without triggering the TextWatcher.
     *
     * @param etName The EditText view containing the folder name input.
     * @param newName The new folder name to display.
     */
    private fun modifyFolderName(etName: EditText, newName:String){
        etName.apply {
            removeTextChangedListener(folderNameWatcher)
            val currentSelection = selectionStart
            setText(newName)
            setSelection(currentSelection, newName.length)
            addTextChangedListener(folderNameWatcher)
        }
    }
    /**
     * Configures the "Create Folder" button with click logic:
     * - Validates the folder name.
     * - Checks for length limits and non-empty value.
     * - If valid, triggers the ViewModel to check for duplicates.
     *
     * @param binding The FragmentAddFolderBinding instance for accessing the UI.
     */
    private fun configureButtonAddFolder(binding: FragmentAddFolderBinding) {
        binding.btnCreateFolder.setOnClickListener{
            //call to addFolder
            val name = binding.folderNameInput.text
            val color = getString(colorSelected.second)
            Log.d(CREATE_FOLDER_TAG, "Folder: with name $name and color $color was created")

            val currentFolderName = binding.folderNameInput.text.toString().trim()
            if(currentFolderName.isNotEmpty()){
                if(currentFolderName.length > MAX_FOLDER_NAME_LENGTH)
                    binding.folderName.setError(getString(R.string.too_long_folder_name))
                else
                    addFolderViewModel.checkIfFolderNameExists()
            }else{
                binding.folderName.setError(getString(R.string.error_blank_folder_name))
            }
        }
    }
    /**
     * Configures the spinner for selecting the folder color.
     * Updates the ViewModel with the selected color.
     *
     * @param spFolderColors The Spinner view used for selecting folder colors.
     */
    private fun configureSpinner(spFolderColors: Spinner){
        spFolderColors.adapter = SpinnerFoldersColorsAdapter(requireContext(),colors)
        spFolderColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                colorSelected = colors[position]
                //communicating to viewmodel
                addFolderViewModel.updateFolderColor(colorSelected.third)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                colorSelected = colors[0]
            }
        }
    }


}