package tfg.uniovi.melodies.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentImportBinding
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.adapters.SpinnerFoldersAdapter
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModel
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModelProviderFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.utils.ShowAlertDialog
import tfg.uniovi.melodies.utils.parser.String2MusicXML
import tfg.uniovi.melodies.utils.parser.XMLParser

private const val IMPORT_TAG = "IMPORT"
private const val EXTENSION_APP_XML = "application/xml"
private const val EXTENSION_TXT_XML = "text/xml"

class Import : Fragment() {
    private lateinit var binding : FragmentImportBinding
    private val openMultipleMusicXmlLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
            uris.forEachIndexed { index, uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val xmlContent = inputStream?.bufferedReader()?.use { it.readText() }

                if (xmlContent != null) {
                    try {
                        val doc = String2MusicXML.string2doc(xmlContent)
                        val parser = XMLParser(doc)
                        importViewModel.addMusicXMLSheet(xmlContent,
                            parser.findNameTitle(requireContext(),doc),
                            parser.findAuthor(requireContext(),doc))
                    } catch (e: Exception) {
                        ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                            requireContext(),
                            getString(R.string.alert_dialog_title_error_parsing),
                            e.message!!,
                            IMPORT_TAG,
                            "Error parsing XML to import (archivo $index): ${e.message}"
                        )
                    }
                } else {
                    ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                        requireContext(),
                        getString(R.string.alert_dialog_title_error_parsing),
                        getString(R.string.alert_dialog_msg_error_parsing),
                        IMPORT_TAG,
                        "Error parsing XML to import (archivo $index): xml was null/empty"
                    )
                }
            }
        }


    private lateinit var importViewModel: ImportViewModel
    private var folderChosen: Folder? = null
    private lateinit var folders:List<Folder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportBinding.inflate(inflater, container, false)
        imageButtonSetUp()
        binding.spFolder.adapter =  SpinnerFoldersAdapter(requireContext(), mutableListOf())
        viewModelSetUp()
        btnImportSetUp()
        viewModelFolderChosenSetUp()
        configureSpinner()
        return binding.root
    }

    private fun viewModelFolderChosenSetUp() {
        importViewModel.folderChosen.observe(viewLifecycleOwner) { newFolderChosen ->
            folderChosen = newFolderChosen

            val folderList = folders
            val index = folderList.indexOfFirst { it.folderId == newFolderChosen.folderId }

            if (index != -1) {
                binding.spFolder.setSelection(index)
            }
        }
    }

    private fun btnImportSetUp() {
        binding.btnImport.setOnClickListener {
            val result = importViewModel.storeNewMusicXML()
            if (folderChosen != null && importViewModel.musicXMLSheets.value?.isEmpty() == false) {
                if (result) {
                    Log.d(IMPORT_TAG, "New musicxml has been added to ${folderChosen!!.name}")
                    Toast.makeText(
                        context,
                        getString(R.string.import_successful),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    findNavController().navigate(R.id.action_importing_to_home_fragment)
                    //importViewModel.cleanMusicXMLSheets()
                } else {
                    Log.d(IMPORT_TAG, "New musicxml has NOT been added to ${folderChosen!!.name}")
                    Toast.makeText(
                        context,
                        getString(R.string.import_unsuccessful),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                    requireContext(),
                    getString(R.string.no_import),
                    getString(R.string.no_folders_no_import),
                    IMPORT_TAG, getString(R.string.import_unsuccessful)
                )
            }
            importViewModel.cleanMusicXMLSheets()
        }
    }

    private fun viewModelSetUp() {
        importViewModel = ViewModelProvider(
            this, ImportViewModelProviderFactory(
                PreferenceManager.getUserId(requireContext())!!
            )
        )[ImportViewModel::class.java]
        importViewModel.folders.observe(viewLifecycleOwner) { folders ->
            this.folders = folders
            val adapter = binding.spFolder.adapter as SpinnerFoldersAdapter
            adapter.updateFolders(folders)
        }
        importViewModel.loadFolders()
        importViewModel.musicXMLSheets.observe(viewLifecycleOwner) { sheets ->
            if(sheets.isEmpty())
                binding.tvNameOfFiles.text = getString(R.string.import_name_files)
            else{
                binding.tvNameOfFiles.text = ""
                var fileNames = ""
                for (sheet in sheets) {
                    fileNames += sheet.name + "\n"
                }
                binding.tvNameOfFiles.text = fileNames
            }
        }


    }

    private fun imageButtonSetUp() {
        binding.imgBtnUploadFile.setOnClickListener {
            openMultipleMusicXmlLauncher.launch(
                arrayOf(EXTENSION_APP_XML, EXTENSION_TXT_XML)
            )
        }
    }
    private fun configureSpinner(){
        binding.spFolder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                folderChosen = folders[position]
                importViewModel.updateFolderChosen(folderChosen!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                folderChosen = folders[0]
            }
        }
    }



}