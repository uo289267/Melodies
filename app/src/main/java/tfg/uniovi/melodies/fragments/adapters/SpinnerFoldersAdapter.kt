package tfg.uniovi.melodies.fragments.adapters

import android.content.Context
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.fragments.viewmodels.ImportViewModel

class SpinnerFoldersAdapter(context: Context, var folders: MutableList<Folder>): ArrayAdapter<Folder>(context, R.layout.spinner_folder_item, folders) {

    fun updateFolders(newFolders: List<Folder>){
//        val oldSize = folders.size
//        this.folders.clear()
//        folders.addAll(newFolders)
//        //items beyond the newsize need to be removed when the oldsize is greater than the newsize
//        if(oldSize > folders.size)
//            notifyItemRangeRemoved(folders.size, oldSize - folders.size)
//        //all items need to be updated
//        notifyItemRangeChanged(0, folderList.size)
        folders = newFolders.toMutableList()
        notifyDataSetChanged()

    }
}