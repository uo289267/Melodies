package tfg.uniovi.melodies.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentImportBinding
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.SpinnerFoldersAdapter
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModel
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModelProviderFactory
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModel
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModelProviderFactory
import java.util.UUID

class Import : Fragment() {
    private lateinit var binding : FragmentImportBinding
    private val openMultipleMusicXmlLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
            uris.forEach { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val xmlContent = inputStream?.bufferedReader()?.use { it.readText() }

                // Aquí puedes cargar el archivo con Verovio o almacenarlo en una lista
                Log.d("MusicXML", "Archivo leído: ${uri.lastPathSegment}")
                Log.d("MusicXML", xmlContent ?: "Archivo vacío")
            }
        }

    private lateinit var importViewModel: ImportViewModel
    private lateinit var folderChosen: Folder
    private lateinit var folders:List<Folder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportBinding.inflate(inflater, container, false)
        binding.imgBtnImport.setOnClickListener{
            openMultipleMusicXmlLauncher.launch(
                arrayOf("application/xml", "application/vnd.recordare.musicxml+xml", "text/xml")
            )
        }
        configureSpinner()
        importViewModel = ViewModelProvider(this, ImportViewModelProviderFactory(
            UUID.fromString("a5ba172c-39d8-4181-9b79-76b8f23b5d18")
        )).get(ImportViewModel::class.java)
        importViewModel.folders.observe(viewLifecycleOwner){
            folders ->
            this.folders = folders
            val adapter = binding.spFolder.adapter as SpinnerFoldersAdapter
            adapter.updateFolders(folders)
        }
        importViewModel.loadFolders()

        return binding.root
    }


    private fun configureSpinner(){
        binding.spFolder.adapter =  SpinnerFoldersAdapter(requireContext(), mutableListOf())
        binding.spFolder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                folderChosen = folders[position]
                //communicating to viewmodel
                importViewModel.updateFolderChosen(folderChosen)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                folderChosen = folders[0]
            }
        }
    }
}