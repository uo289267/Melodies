package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

/**
 * Fragment that displays the full music library organized by folders.
 * Provides search functionality to filter folders and sheets,
 * and navigation to the sheet visualization screen.
 */
class FullLibrary : Fragment() {
    private lateinit var binding: FragmentFullLibraryBinding
    private lateinit var fullLibraryViewModel: FullLibraryViewModel
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
        ViewModelProvider(this, factory)["LibraryViewModel_$folderId", LibraryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFullLibraryBinding.inflate(inflater, container, false)
        val folderList = emptyList<Folder>()
        fullLibraryViewModel = ViewModelProvider(this, FullLibraryViewModelProviderFactory(
            PreferenceManager.getUserId(requireContext())!!))[FullLibraryViewModel::class.java]
        val onLongClickRename = {sheetIdNFolderId : SheetVisualizationDto, newName : String ->
            fullLibraryViewModel.renameSheet(sheetIdNFolderId.sheetId, sheetIdNFolderId.folderId, newName)
            fullLibraryViewModel.loadFolders()
        }
        adapter = FolderInFullLibraryAdapter(folderList,navigationFunction, this,
            libraryViewModelProviderFactory)
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
        fullLibraryViewModel.folders.observe(viewLifecycleOwner){
            list ->
            allFolders = list
            adapter.updateFullLibrary(list)
            if(allFolders.isNotEmpty()){
                binding.tvNoSongs.visibility = View.GONE
                binding.recyclerViewFullLibrary.visibility = View.VISIBLE
            }else{
                binding.tvNoSongs.visibility = View.VISIBLE
                binding.recyclerViewFullLibrary.visibility =View.GONE
            }

        }
        fullLibraryViewModel.loadFolders()
        searchMenuSetUp()
    }
    /**
     * Configures the search menu in the toolbar.
     * Adds a MenuProvider that handles search text changes and submission.
     */
    private fun searchMenuSetUp() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.library_search_menu, menu)

                val searchItem = menu.findItem(R.id.app_bar_search)
                val searchView = searchItem.actionView as SearchView

                searchView.queryHint = getString(R.string.search_hint)

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return handleFoldersForSearch(query)
                    }
                    override fun onQueryTextChange(newText: String?): Boolean {
                        return handleFoldersForSearch(newText)
                    }
                })
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    /**
     * Filters the list of folders based on the search query.
     * Matches folder names, sheet names, or sheet authors.
     *
     * @param query The search text entered by the user. May be null or empty.
     * @return Always returns true to indicate the query was handled.
     */
    private fun handleFoldersForSearch(query: String?): Boolean {
        val filteredFolders = allFolders.filter { folder ->
            val searchText = query.orEmpty().trim()
            if (searchText.isEmpty()) {
                adapter.updateFullLibrary(allFolders)
                return true
            }
            val matchesFolderName = folder.name.contains(searchText, ignoreCase = true)

            val libraryViewModel = libraryViewModelProviderFactory(folder.folderId)
            val sheets = libraryViewModel.sheets.value ?: emptyList()
            val matchesSheetName = sheets.any { sheet ->
                sheet.name.contains(searchText, ignoreCase = true)||
                        sheet.author.contains(searchText, ignoreCase = true)
            }

            matchesFolderName || matchesSheetName
        }

        adapter.updateFullLibrary(filteredFolders)

        return true
    }
}