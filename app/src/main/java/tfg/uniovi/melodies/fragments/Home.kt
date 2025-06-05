package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import tfg.uniovi.melodies.databinding.FragmentHomeBinding
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.FolderAdapter
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModel
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModelProviderFactory
import tfg.uniovi.melodies.preferences.PreferenceManager
import java.util.UUID

class Home : Fragment() {
    private lateinit var  binding: FragmentHomeBinding
    private lateinit var folderViewModel : FolderViewModel
    private val navigationFunction = {folderId: String ->
        run{
            val destination = HomeDirections.actionHomeFragmentToLibrary(folderId)
            findNavController().navigate(destination)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        folderViewModel = ViewModelProvider(this, FolderViewModelProviderFactory(
            PreferenceManager.getUserId(requireContext())!!
        ))[FolderViewModel::class.java]
        binding.fabAddNewFolder.setOnClickListener{
            val destination = HomeDirections.actionHomeFragmentToAddFolder()
            findNavController().navigate(destination)
        }
        val gridLayoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = gridLayoutManager

        val folderList = emptyList<Folder>()
        // Adapter
        binding.recyclerView.adapter = FolderAdapter(folderList, navigationFunction, folderViewModel ,viewLifecycleOwner)

        return binding.root

    }



}