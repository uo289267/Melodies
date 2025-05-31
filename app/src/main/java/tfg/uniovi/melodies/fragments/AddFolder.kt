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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentAddFolderBinding
import tfg.uniovi.melodies.fragments.adapters.SpinnerFoldersColorsAdapter
import tfg.uniovi.melodies.fragments.viewmodels.AddFolderViewModel
import tfg.uniovi.melodies.fragments.viewmodels.AddFolderViewModelProviderFactory
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModel
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModelProviderFactory
import tfg.uniovi.melodies.utils.TextWatcherAdapter
import java.util.UUID

class AddFolder : Fragment() {

    private lateinit var  binding: FragmentAddFolderBinding
    private lateinit var foldersViewModel: FolderViewModel
    private lateinit var addFolderViewModel: AddFolderViewModel
    private val folderViewModel: FolderViewModel by viewModels()

    private val folderNameWatcher = object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            //communicating to viewmodel
            this@AddFolder.addFolderViewModel.updateFolderName(s.toString())
        }
    }
    private val colors = listOf(
        Pair(R.drawable.folder_yellow,R.string.yellow),
        Pair(R.drawable.folder_pink,R.string.pink),
        Pair(R.drawable.folder_blue, R.string.blue)
    )
    private var colorSelected: Int = colors[0].first

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //FolderViewModelProvider
        val owner = findNavController().getViewModelStoreOwner(R.id.navigation)
        val factory = FolderViewModelProviderFactory( UUID.fromString("a5ba172c-39d8-4181-9b79-76b8f23b5d18"))
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
            UUID.fromString("a5ba172c-39d8-4181-9b79-76b8f23b5d18")
        )
        ).get(AddFolderViewModel::class.java)

        addFolderViewModel.folderDTO.observe(viewLifecycleOwner){dto ->
            modifyFolderName(binding.folderNameInput, dto.name)
        }

        binding.toolbar.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return binding.root
    }

    private fun modifyFolderName(etName: EditText, newName:String){
        etName.apply {
            removeTextChangedListener(folderNameWatcher)
            val currentSelection = selectionStart
            setText(newName)
            setSelection(currentSelection, newName.length)
            addTextChangedListener(folderNameWatcher)
        }
    }
    private fun configureButtonAddFolder(binding: FragmentAddFolderBinding) {
        binding.btnCreateFolder.setOnClickListener{
            //call to addFolder
            val name = binding.folderNameInput.text
            val color = colorSelected
            Log.d("FOLDER_CREATION", "Folder: with name $name and color $color was created")

            //communicating to viewmodel
            if(binding.folderNameInput.text.isNullOrEmpty())
                binding.folderName.setError(getString(R.string.error_blank_folder_name))
            else if(binding.folderNameInput.text!!.length > 30)
                binding.folderName.setError(getString(R.string.too_long_folder_name))
            else{
                addFolderViewModel.createFolder()
                findNavController().popBackStack()
            }


        }
    }

    private fun configureSpinner(spFolderColors: Spinner){
        spFolderColors.adapter = SpinnerFoldersColorsAdapter(requireContext(),colors)
        spFolderColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                colorSelected = colors[position].first
                //communicating to viewmodel
                addFolderViewModel.updateFolderColor(colorSelected)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                colorSelected = colors[0].first
            }
        }
    }


}