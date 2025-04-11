package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.databinding.FragmentHomeBinding
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.FolderAdapter
import tfg.uniovi.melodies.repositories.FolderFirestore

class Home : Fragment() {
    private lateinit var  binding: FragmentHomeBinding
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
        binding.fabAddNewFolder.setOnClickListener{
            val destination = HomeDirections.actionHomeFragmentToAddFolder()
            findNavController().navigate(destination)
        }
        val repo = FolderFirestore()
        var folderList = emptyList<Folder>()
        lifecycleScope.launch {
             folderList= repo.getAllFolders()
            // Layout manager
            val gridLayoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
            binding.recyclerView.layoutManager = gridLayoutManager

            // Adapter
            binding.recyclerView.adapter = FolderAdapter(folderList, navigationFunction, viewLifecycleOwner)


        }
        return binding.root

    }


}