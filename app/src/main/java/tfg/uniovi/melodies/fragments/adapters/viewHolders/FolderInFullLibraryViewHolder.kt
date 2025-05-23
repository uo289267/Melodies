package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.SheetInFolderAdapter
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.utils.RecyclerViewItemDecoration

class FolderInFullLibraryViewHolder (
    private val view: View,
    private val navigateFunction: (String)-> Unit,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.ViewHolder (view) {
    private var tvFolderTitle : TextView = view.findViewById(R.id.tv_folder_title)
    var recyclerSongsPerFolder : RecyclerView = view.findViewById(R.id.recycler_sheets_in_folder)
    fun bind(folder: Folder, libraryViewModel: LibraryViewModel){
        tvFolderTitle.text = folder.name
        val adapter = SheetInFolderAdapter( emptyList(), navigateFunction, libraryViewModel)
        recyclerSongsPerFolder.adapter =  adapter
        recyclerSongsPerFolder.layoutManager = LinearLayoutManager(view.context)
        recyclerSongsPerFolder.addItemDecoration(RecyclerViewItemDecoration(view.context
            , R.drawable.divider))
        recyclerSongsPerFolder.setHasFixedSize(false)
        recyclerSongsPerFolder.isNestedScrollingEnabled = false
        libraryViewModel.sheets.observe(lifecycleOwner) { list ->
            adapter.updateSheets(list)
        }
        libraryViewModel.loadSheets()

    }

}
