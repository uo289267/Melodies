package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
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
        binding.recyclerViewLibrary.adapter = SheetAdapter(sheetList,navFunc,libraryViewModel ,viewLifecycleOwner )
        binding.recyclerViewLibrary.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLibrary.addItemDecoration(RecyclerViewItemDecoration(requireContext(), R.drawable.divider))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }


}