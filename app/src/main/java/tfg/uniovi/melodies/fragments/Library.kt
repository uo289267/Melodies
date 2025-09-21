package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentLibraryBinding
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.adapters.SheetInFolderAdapter
import tfg.uniovi.melodies.fragments.adapters.touchHelpers.MyItemTouchHelper
import tfg.uniovi.melodies.fragments.adapters.viewHolders.DELETE
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModelProviderFactory
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.utils.RecyclerViewItemDecoration
import tfg.uniovi.melodies.utils.SheetItemToucherHelper

/**
 * Fragment that displays all MusicXML sheets within a selected folder.
 * Provides functionalities such as viewing, renaming, deleting sheets,
 * and searching within the folder's sheets.
 */
class Library : Fragment() {

    private lateinit var binding: FragmentLibraryBinding
    private lateinit var libraryViewModel: LibraryViewModel
    private val args : LibraryArgs by navArgs()
    private lateinit var adapter : SheetInFolderAdapter
    private var allSheetsInFolder = listOf<MusicXMLSheet>()
    private val navigationFunction = {dto: SheetVisualizationDto ->
        run{
            val destination = LibraryDirections.actionLibraryToSheetVisualization(dto)
            findNavController().navigate(destination)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        val sheetList = emptyList<MusicXMLSheet>()
        libraryViewModel = ViewModelProvider(this, LibraryViewModelProviderFactory(
            PreferenceManager.getUserId(requireContext())!!, args.folderId
        ))[LibraryViewModel::class.java]
        binding.fabImportSheet.setOnClickListener{
            val destination = LibraryDirections.actionLibraryToImporting(args.folderId)
            findNavController().navigate(destination)
        }

        val onLongClickRename = {sheetIdNFolderId : SheetVisualizationDto, newName : String ->
            libraryViewModel.renameSheet(sheetIdNFolderId.sheetId, sheetIdNFolderId.folderId, newName)
            libraryViewModel.loadSheets()
        }
        val onDelete = { dto: SheetVisualizationDto ->
            libraryViewModel.deleteSheet(dto.sheetId, dto.folderId)
        }

        adapter = SheetInFolderAdapter(sheetList, navigationFunction, onLongClickRename, onDelete)
        binding.recyclerViewLibrary.adapter = adapter
        val itemTouchHelper = SheetItemToucherHelper.create(adapter, libraryViewModel, requireContext())
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewLibrary)


        binding.recyclerViewLibrary.adapter = adapter
        binding.recyclerViewLibrary.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLibrary.addItemDecoration(RecyclerViewItemDecoration(requireContext(), R.drawable.divider))

        val toolbar = binding.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        libraryViewModel.sheets.observe(viewLifecycleOwner) { list ->
            allSheetsInFolder = list
            adapter.updateSheets(allSheetsInFolder)
            if(allSheetsInFolder.isNotEmpty()){
                binding.tvNoSongs.visibility = View.GONE
                binding.recyclerViewLibrary.visibility = View.VISIBLE
            }else{
                binding.tvNoSongs.visibility = View.VISIBLE
                binding.recyclerViewLibrary.visibility =View.GONE
            }
        }
        libraryViewModel.loadSheets()
        libraryViewModel.folderName.observe(viewLifecycleOwner) { name ->
            binding.toolbar.title = name
        }
        libraryViewModel.loadFolderName()

        searchMenuSetUp()
    }
    /**
     * Sets up the search menu with a SearchView to filter sheets by name or author.
     * Adds the menu provider to the hosting activity.
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
                        return handleSheetsForSearch(query)
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return handleSheetsForSearch(newText)
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    /**
     * Filters the sheets list based on the search query entered by the user.
     *
     * @param newText The current text query input by the user.
     * @return Boolean Always returns true to indicate the query was handled.
     */
    private fun handleSheetsForSearch(newText: String?): Boolean {
        val filteredList = allSheetsInFolder.filter { sheet ->
            sheet.name.contains(newText.orEmpty(), ignoreCase = true) ||
                    sheet.author.contains(newText.orEmpty(), ignoreCase = true)
        }
        adapter.updateSheets(filteredList)
        return true
    }


}