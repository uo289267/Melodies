package tfg.uniovi.melodies.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Button
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentAddFolderBinding
import tfg.uniovi.melodies.databinding.FragmentHomeBinding
import tfg.uniovi.melodies.fragments.adapters.SpinnerAdapter
import tfg.uniovi.melodies.fragments.viewmodels.AddFolderViewModel

class AddFolder : Fragment() {

    private lateinit var  binding: FragmentAddFolderBinding

    private val viewModel: AddFolderViewModel by viewModels()


    private val colors = listOf(
        Pair(R.drawable.folder,R.string.yellow),
        Pair(R.drawable.folder_pink,R.string.pink),
        Pair(R.drawable.folder_blue, R.string.blue)


    )
    private var colorSelected: Int = colors[0].first

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFolderBinding.inflate(inflater, container, false)

        configureSpinner(binding.spFolderColors)
        configureButtonAddFolder(binding.btnCreateFolder)

        return binding.root
    }

    private fun configureButtonAddFolder(btnCreateFolder: Button) {
        btnCreateFolder.setOnClickListener{
            //call to addFolder
            var name = binding.folderNameInput.text
            var color = colorSelected
            Log.d("FOLDER_cREATION", "Folder: with name $name and color $color")
        }
    }

    private fun configureSpinner(spFolderColors: Spinner){
        spFolderColors.adapter = SpinnerAdapter(requireContext(),colors);
        spFolderColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                colorSelected = colors[position].first
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                colorSelected = colors[0].first
            }
        }
    }
}