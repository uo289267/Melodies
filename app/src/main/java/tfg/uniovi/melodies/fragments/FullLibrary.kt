package tfg.uniovi.melodies.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentFullLibraryBinding
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.FolderInFullLibraryAdapter
import tfg.uniovi.melodies.fragments.viewmodels.FullLibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.FullLibraryViewModelProviderFactory
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModelProviderFactory
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto
import tfg.uniovi.melodies.preferences.PreferenceManager


class FullLibrary : Fragment() {
    private lateinit var binding: FragmentFullLibraryBinding
    private lateinit var libraryViewModel: FullLibraryViewModel
    private val args : LibraryArgs by navArgs()
    private lateinit var adapter: FolderInFullLibraryAdapter
    private var allFolders = listOf<Folder>()
    private val navigationFunction = {dto: SheetVisualizationDto ->
        run{
            val destination = FullLibraryDirections.actionFullLibraryToSheetVisualization(dto)
            findNavController().navigate(destination)
        }
    }
    private val libraryViewModelProviderFactory = { folderId: String ->
        val factory = LibraryViewModelProviderFactory(
            PreferenceManager.getUserId(requireContext())!!,
            folderId
        )
        ViewModelProvider(this, factory).get("LibraryViewModel_$folderId", LibraryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFullLibraryBinding.inflate(inflater, container, false)
        val folderList = emptyList<Folder>()
        libraryViewModel = ViewModelProvider(this, FullLibraryViewModelProviderFactory(
            PreferenceManager.getUserId(requireContext())!!))[FullLibraryViewModel::class.java]
        adapter = FolderInFullLibraryAdapter(folderList,navigationFunction,libraryViewModel, this, libraryViewModelProviderFactory)
        binding.recyclerViewFullLibrary.adapter = adapter
        binding.recyclerViewFullLibrary.layoutManager = LinearLayoutManager(context)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


}