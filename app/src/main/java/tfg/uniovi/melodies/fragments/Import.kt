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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.w3c.dom.Document
import org.w3c.dom.Element
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentImportBinding
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.SpinnerFoldersAdapter
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModel
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModelProviderFactory
import tfg.uniovi.melodies.utils.ShowAlertDialog
import tfg.uniovi.melodies.utils.parser.String2MusicXML
import java.util.UUID

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
                        importViewModel.addMusicXMLSheet(xmlContent, findNameTitle(doc), findAuthor(doc))
                    } catch (e: Exception) {
                        ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                            requireContext(),
                            getString(R.string.alert_dialog_title_error_parsing),
                            e.message!!,
                            "IMPORT",
                            "Error parsing XML to import (archivo $index): ${e.message}"
                        )
                    }
                } else {
                    ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                        requireContext(),
                        getString(R.string.alert_dialog_title_error_parsing),
                        getString(R.string.alert_dialog_msg_error_parsing),
                        "IMPORT",
                        "Error parsing XML to import (archivo $index): xml was null/empty"
                    )
                }

                // Aquí puedes cargar el archivo con Verovio o almacenarlo en una lista
                Log.d("MusicXML", "Archivo $index leído: ${uri.lastPathSegment}")
                Log.d("MusicXML", xmlContent ?: "Archivo $index vacío")
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
        binding.imgBtnUploadFile.setOnClickListener{
            openMultipleMusicXmlLauncher.launch(
                arrayOf("application/xml", "application/vnd.recordare.musicxml+xml", "text/xml")
            )
        }
        binding.spFolder.adapter =  SpinnerFoldersAdapter(requireContext(), mutableListOf())
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
        importViewModel.musicXMLSheets.observe(viewLifecycleOwner){ sheets ->
            binding.tvNameOfFiles.text=""
            var fileNames = ""
            for (sheet in sheets){
                fileNames+= sheet.name + " "
            }
            binding.tvNameOfFiles.text=fileNames
        }

        binding.btnImport.setOnClickListener {
            val result = importViewModel.storeNewMusicXML()
            if(result){

                Log.d("IMPORT", "New musicxml has been added to ${folderChosen.name}")
                Toast.makeText(context, getString(R.string.import_successful), Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(R.id.action_importing_to_home_fragment)
            }else{
                Log.d("IMPORT", "New musicxml has NOT been added to ${folderChosen.name}")
                Toast.makeText(context, getString(R.string.import_unsuccessful), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        importViewModel.folderChosen.observe(viewLifecycleOwner) { newFolderChosen ->
            folderChosen = newFolderChosen

            val folderList = folders
            val index = folderList.indexOfFirst { it.folderId == newFolderChosen.folderId }

            if (index != -1) {
                binding.spFolder.setSelection(index)
            }
        }

        configureSpinner()
        return binding.root
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
                importViewModel.updateFolderChosen(folderChosen)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                folderChosen = folders[0]
            }
        }
    }

    private fun findAuthor(xmlDocument: Document) : String{
        val nodeList = xmlDocument.getElementsByTagName("creator")
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i) as Element
            if (node.getAttribute("type") == "composer") {
                return node.textContent
            }
        }
        return getString(R.string.anonymous)
    }

    private fun findNameTitle(xmlDocument: Document):String{
        val workNodes = xmlDocument.getElementsByTagName("work-title")
        if (workNodes.length > 0) {
            return workNodes.item(0).textContent
        }
        return getString(R.string.unknown_name)

    }

}