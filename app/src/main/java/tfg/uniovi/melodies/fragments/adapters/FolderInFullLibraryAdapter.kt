package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.viewHolders.FolderInFullLibraryViewHolder
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto

class FolderInFullLibraryAdapter : RecyclerView.Adapter<FolderInFullLibraryViewHolder> {
    private val folderList : MutableList<Folder>
    private val navigateFunction: (SheetVisualizationDto) -> Unit
    private val libraryViewModelProviderFactory : (String) -> LibraryViewModel
    private val viewPool = RecyclerView.RecycledViewPool()
    private val lifecycleOwner : LifecycleOwner
    private val onLongClickRename: (SheetVisualizationDto, String) -> Unit


    constructor(
        folderList: List<Folder>,
        navigateFunction: (SheetVisualizationDto) -> Unit,
        lifecycleOwner: LifecycleOwner,
        libraryViewModelProviderFactory: (String) -> LibraryViewModel,
        onLongClickRename: (SheetVisualizationDto, String) -> Unit
    ){
        this.folderList = folderList.toMutableList()
        this.navigateFunction = navigateFunction
        this.libraryViewModelProviderFactory = libraryViewModelProviderFactory
        this.lifecycleOwner = lifecycleOwner
        this.onLongClickRename = onLongClickRename
    }

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
        return FolderInFullLibraryViewHolder(view, navigateFunction, lifecycleOwner, onLongClickRename)
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