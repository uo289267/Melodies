package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.viewHolders.FolderInFullLibraryViewHolder
import tfg.uniovi.melodies.fragments.viewmodels.FullLibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel

class FolderInFullLibraryAdapter : RecyclerView.Adapter<FolderInFullLibraryViewHolder> {
    private val folderList : MutableList<Folder>
    private val navigateFunction: (String) -> Unit
    private val viewModel : FullLibraryViewModel
    private val libraryViewModelProviderFactory : (String) -> ViewModelProvider
    private val viewPool = RecyclerView.RecycledViewPool()
    private val lifecycleOwner : LifecycleOwner

    constructor(folderList: List<Folder>,
                navigateFunction: (String) -> Unit,
                viewModel : FullLibraryViewModel,
                lifecycleOwner: LifecycleOwner,
                libraryViewModelProviderFactory : (String) -> ViewModelProvider){
        this.folderList = folderList.toMutableList()
        this.navigateFunction = navigateFunction
        this.viewModel = viewModel
        this.viewModel.folder.observe(lifecycleOwner){
            list -> updateFullLibrary(list)
        }
        this.libraryViewModelProviderFactory = libraryViewModelProviderFactory
        this.viewModel.loadFolders()
        this.lifecycleOwner = lifecycleOwner
    }

    private fun updateFullLibrary(newFolders: List<Folder>){
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
        val libraryViewModel = libraryViewModelProviderFactory(folder.folderId).get(LibraryViewModel::class.java)
        viewHolder.bind(folder, libraryViewModel)
        viewHolder.recyclerSongsPerFolder.setRecycledViewPool(viewPool)
    }

    override fun getItemCount(): Int {
        return folderList.size
    }





}