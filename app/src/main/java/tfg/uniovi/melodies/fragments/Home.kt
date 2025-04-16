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
import java.util.UUID

class Home : Fragment() {
    private lateinit var  binding: FragmentHomeBinding
    private lateinit var folderViewModel : FolderViewModel
    private val navigationFunction = {folderId: String ->
        run{
            val destino = HomeDirections.actionHomeFragmentToLibrary(folderId)
            findNavController().navigate(destino)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        //fab
        folderViewModel = ViewModelProvider(this, FolderViewModelProviderFactory(
            UUID.fromString("a5ba172c-39d8-4181-9b79-76b8f23b5d18")
        )).get(FolderViewModel::class.java)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}