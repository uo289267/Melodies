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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentLibraryBinding
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.adapters.SheetAdapter
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModelProviderFactory
import tfg.uniovi.melodies.utils.RecyclerViewItemDecoration
import java.util.UUID

/**
 * A simple [Fragment] subclass.
 * Use the [Library.newInstance] factory method to
 * create an instance of this fragment.
 */
class Library : Fragment() {

    private lateinit var binding: FragmentLibraryBinding
    private lateinit var libraryViewModel: LibraryViewModel
    private val args : LibraryArgs by navArgs()
    private lateinit var  adapter:SheetAdapter
    private var allSheets = listOf<MusicXMLSheet>()
    private val navigationFunction =  { dest: String -> println(dest) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        val sheetList = emptyList<MusicXMLSheet>()
        libraryViewModel = ViewModelProvider(this, LibraryViewModelProviderFactory(
            UUID.fromString("a5ba172c-39d8-4181-9b79-76b8f23b5d18"), args.folderId
        )).get(LibraryViewModel::class.java)

        val navFunc = { dest: String -> println(dest) }
        adapter = SheetAdapter(sheetList,navFunc,libraryViewModel ,viewLifecycleOwner )

        binding.recyclerViewLibrary.adapter = adapter
        binding.recyclerViewLibrary.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLibrary.addItemDecoration(RecyclerViewItemDecoration(requireContext(), R.drawable.divider))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        libraryViewModel.sheets.observe(viewLifecycleOwner) { list ->
            allSheets = list
            adapter.updateSheets(allSheets)
        }
        libraryViewModel.loadSheets()
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val menuHost : MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.library, menu)

                val searchItem = menu.findItem(R.id.app_bar_search)
                val searchView = searchItem.actionView as SearchView

                searchView.queryHint = "Search ..."

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        // Handle search submit
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val filteredList = allSheets.filter { sheet ->
                            sheet.name.contains(newText.orEmpty(), ignoreCase = true)
                        }
                        adapter.updateSheets(filteredList)
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }



}