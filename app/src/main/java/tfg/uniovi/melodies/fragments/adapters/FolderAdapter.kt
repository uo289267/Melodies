package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.adapters.viewHolders.FolderViewHolder
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModel

class FolderAdapter : RecyclerView.Adapter<FolderViewHolder> {
    private val folderList : MutableList<Folder>
    private val navigateFunction: (String) -> Unit
    private val viewModel : FolderViewModel
    private val onLongClickDelete: (Folder) -> Unit
    constructor(folderList: List<Folder>,
                navigateFunction: (String) -> Unit
                , viewModel: FolderViewModel
                ,lifecycleOwner: LifecycleOwner){
        this.folderList = folderList.toMutableList()
        this.navigateFunction = navigateFunction
        this.viewModel = viewModel
        this.viewModel.folders.observe(lifecycleOwner){
            list -> updateFolders(list)
        }
        this.viewModel.loadFolders()
        this.onLongClickDelete = {folder ->
            viewModel.deleteFolder(folder.folderId)
        }
    }

    private fun updateFolders(newFolders: List<Folder>) {
        val oldSize = folderList.size
        folderList.clear()
        folderList.addAll(newFolders)
        //items beyond the newsize need to be removed when the oldsize is greater than the newsize
        if(oldSize > folderList.size)
            notifyItemRangeRemoved(folderList.size, oldSize - folderList.size)
        //all items need to be updated
        notifyItemRangeChanged(0, folderList.size)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FolderViewHolder {
        val layout = R.layout.recycler_folder_item
        val view = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return FolderViewHolder(view, navigateFunction,onLongClickDelete )
    }

    override fun onBindViewHolder(viewHolder: FolderViewHolder, position: Int) {
        viewHolder.bind(folderList[position])
    }
    override fun getItemCount(): Int {
        return folderList.size
    }
}