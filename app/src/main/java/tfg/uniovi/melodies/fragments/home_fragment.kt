package tfg.uniovi.melodies.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.databinding.FragmentHomeBinding
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.FolderAdapter
import java.util.UUID

class home_fragment : Fragment() {
private lateinit var recyclerViewFolders: RecyclerView
private lateinit var  binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        //TODO quitar
        val owner1 = UUID.randomUUID()
        val owner2 = UUID.randomUUID()

        val folderList = listOf(
            Folder("Documentosssssssssssssssssssssssssssssssssssssssssss", "yellow", Timestamp.now(), owner1, UUID.randomUUID()),
            Folder("Fotos1222222222223333333333444444444444444555555555555666666666666666666777777777777778888888888888", "pink", Timestamp.now(), owner2, UUID.randomUUID()),
            Folder("123456789101112131415161718192021222324252627282930", "blue", Timestamp.now(), owner1, UUID.randomUUID()),
            Folder("Videos", "yellow", Timestamp.now(), owner2, UUID.randomUUID()),
            Folder("Proyectos", "pink", Timestamp.now(), owner1, UUID.randomUUID())
        )

        // Layout manager
        val gridLayoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = gridLayoutManager

        // Adapter
        val navFunc = { dest: String -> println(dest) }
        binding.recyclerView.adapter = FolderAdapter(folderList, navFunc, viewLifecycleOwner)

        return binding.root
    }


}