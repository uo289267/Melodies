package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.model.Folder
import tfg.uniovi.melodies.fragments.adapters.viewHolders.FolderInFullLibraryViewHolder
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto

class FolderInFullLibraryAdapter(
    folderList: List<Folder>,
    private val navigateFunction: (SheetVisualizationDto) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
    private val libraryViewModelProviderFactory: (String) -> LibraryViewModel
) : RecyclerView.Adapter<FolderInFullLibraryViewHolder>() {
    private val folderList : MutableList<Folder> = folderList.toMutableList()
    private val viewPool = RecyclerView.RecycledViewPool()

    fun updateFullLibrary(newFolders: List<Folder>){
        val oldSize = folderList.size
        folderList.clear()
        folderList.addAll(newFolders)
        //items beyond the newsize need to be removed when the oldsize is greater than the newsize
        if(oldSize > folderList.size)
            notifyItemRangeRemoved(folderList.size, oldSize - folderList.size)
        //all items need to be updated
        notifyItemRangeChanged(0, folderList.size)
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): FolderInFullLibraryViewHolder {
        val layout = R.layout.recycler_folder_in_full_library_item
        val view = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return FolderInFullLibraryViewHolder(view, navigateFunction, lifecycleOwner)
    }

    override fun onBindViewHolder(viewHolder: FolderInFullLibraryViewHolder, position: Int) {
        val folder = folderList[position]
        val libraryViewModel = libraryViewModelProviderFactory(folder.folderId)
        viewHolder.bind(folder, libraryViewModel)
        viewHolder.recyclerSongsPerFolder.setRecycledViewPool(viewPool)
    }

    override fun getItemCount(): Int {
        return folderList.size
    }





}