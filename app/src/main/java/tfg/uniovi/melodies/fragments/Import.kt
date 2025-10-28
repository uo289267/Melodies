package tfg.uniovi.melodies.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentImportBinding
import tfg.uniovi.melodies.model.Folder
import tfg.uniovi.melodies.fragments.adapters.SpinnerFoldersAdapter
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModel
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModelProviderFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.fragments.utils.ShowAlertDialog
import tfg.uniovi.melodies.processing.parser.String2MusicXML
import tfg.uniovi.melodies.processing.parser.XMLParser

private const val IMPORT_TAG = "IMPORT"
private const val EXTENSION_APP_XML = "application/xml"
private const val EXTENSION_TXT_XML = "text/xml"
const val MAX_LENGTH_SHEET_NAME: Int = 20
/**
 * Fragment responsible for importing one or more MusicXML files into a selected folder.
 * Allows the user to pick files from storage, parse and validate them,
 * choose a destination folder, and save the files through the ViewModel.
 */
class Import : Fragment() {
    private lateinit var binding : FragmentImportBinding
    private val args : ImportArgs by navArgs()
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
                        val title = parser.findNameTitle(requireContext(), doc)
                        val safeTitle = if (title.length > MAX_LENGTH_SHEET_NAME)
                            title.substring(0, MAX_LENGTH_SHEET_NAME)
                        else
                            title
                        importViewModel.addMusicXMLSheet(xmlContent, safeTitle, parser.findAuthor(requireContext(), doc))
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

        super.onCreate(savedInstanceState)
        return binding.root
    }
    /**
     * Observes the selected folder changes in the ViewModel
     * and updates the Spinner selection accordingly.
     */
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
    /**
     * Sets up the import button click listener.
     * Validates if a folder is selected and files are loaded,
     * and triggers the saving process of the MusicXML files.
     *
     */
    private fun btnImportSetUp() {
        binding.btnImport.setOnClickListener {
            val result = importViewModel.storeNewMusicXML()
            if (folderChosen != null && importViewModel.musicXMLSheets.value?.isEmpty() == false) {
                if (result) {
                    Log.d(IMPORT_TAG, "New musicxml has been added to ${folderChosen!!.name}")
                    Toast.makeText(
                        context,
                        importViewModel.musicXMLSheets.value?.size.toString()+" "
                                +getString(R.string.import_successful),
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

    /**
     * Initializes the ViewModel and observes changes in the list of folders and imported files.
     * Updates the Spinner adapter and the displayed file names.
     *
     */
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
            val folderId = args.folderIdImport
            if(folderId.isNotEmpty()){
                folderChosen = folders.find { it.folderId == folderId }
                folderChosen?.let {
                    val index = folders.indexOf(it)
                    if (index != -1){
                        binding.spFolder.setSelection(index)
                        importViewModel.getColorOfSelectedFolder(folderId)
                    }
                    importViewModel.updateFolderChosen(it)
                }
            }


        }
        importViewModel.loadFolders()
        importViewModel.musicXMLSheets.observe(viewLifecycleOwner) { sheets ->
            if(sheets.isEmpty())
                binding.tvNameOfFiles.text = getString(R.string.import_name_files)
            else{
                binding.tvNameOfFiles.movementMethod = ScrollingMovementMethod()
                binding.tvNameOfFiles.isVerticalScrollBarEnabled = true
                binding.tvNameOfFiles.scrollBarStyle=View.SCROLLBARS_OUTSIDE_INSET
                binding.tvNameOfFiles.text = ""
                var fileNames = ""
                for (sheet in sheets) {
                    fileNames += sheet.name + "\n"
                }

                binding.tvNameOfFiles.text = fileNames
            }
        }

        importViewModel.folderChosenColor.observe(viewLifecycleOwner){
            val backgroundColor = Color.parseColor(importViewModel.folderChosenColor.value?.hex)
            binding.btnImport.setBackgroundColor(backgroundColor)

        }

    }
    /**
     * Sets up the upload file button click listener.
     * Launches a file picker to select MusicXML files.
     *
     */
    private fun imageButtonSetUp() {
        binding.imgBtnUploadFile.setOnClickListener {
            openMultipleMusicXmlLauncher.launch(
                arrayOf(EXTENSION_APP_XML, EXTENSION_TXT_XML)
            )
        }
    }
    /**
     * Configures the Spinner that displays available folders.
     * Updates the ViewModel with the newly selected folder.
     *
     */
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
                importViewModel.getColorOfSelectedFolder(folderChosen!!.folderId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                folderChosen = folders[0]
            }
        }
    }



}