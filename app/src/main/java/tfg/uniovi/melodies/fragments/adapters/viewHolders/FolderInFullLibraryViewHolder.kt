package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.SheetInFolderAdapter
import tfg.uniovi.melodies.fragments.adapters.touchHelpers.MyItemTouchHelper
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto
import tfg.uniovi.melodies.utils.RecyclerViewItemDecoration

const val DELETE = "DELETE"

class FolderInFullLibraryViewHolder(
    private val view: View,
    private val navigateFunction: (SheetVisualizationDto) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
    private val onLongClickRename: (SheetVisualizationDto, String) -> Unit

): RecyclerView.ViewHolder (view) {
    private var tvFolderTitle : TextView = view.findViewById(R.id.tv_folder_title)
    var recyclerSongsPerFolder : RecyclerView = view.findViewById(R.id.recycler_sheets_in_folder)
    fun bind(folder: Folder, libraryViewModel: LibraryViewModel){
        tvFolderTitle.text = folder.name
        val adapter = SheetInFolderAdapter( emptyList(), navigateFunction, onLongClickRename)
        recyclerSongsPerFolder.adapter =  adapter
        recyclerSongsPerFolder.layoutManager = LinearLayoutManager(view.context)
        recyclerSongsPerFolder.addItemDecoration(RecyclerViewItemDecoration(view.context
            , R.drawable.divider))
        recyclerSongsPerFolder.setHasFixedSize(false)
        recyclerSongsPerFolder.isNestedScrollingEnabled = false
        libraryViewModel.sheets.observe(lifecycleOwner) { list ->
            adapter.updateSheets(list)
        }
        val itemTouchHelper = ItemTouchHelper(
            MyItemTouchHelper { position, direction ->
                if (direction == ItemTouchHelper.START || direction == ItemTouchHelper.END) {
                    adapter.removeItemAt(position)
                    // notifying vm to remove from db
                    libraryViewModel.deleteSheetAt(position)
                    Log.d(DELETE, "One sheet at position $position was deleted")
                    Toast.makeText(view.context, getString(view.context, R.string.delete_successful), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
        itemTouchHelper.attachToRecyclerView(recyclerSongsPerFolder)
        libraryViewModel.loadSheets()

    }



}
