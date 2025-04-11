package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder

class FolderViewHolder(
    private val view: View,
    private val navigateFunction: (String) -> Unit
): RecyclerView.ViewHolder(view) {
    private val tvFolderTitle: TextView = view.findViewById(R.id.tv_folder_title)
    private val ivFolder: ImageView = view.findViewById(R.id.iv_folder)
    private var currentFolder : Folder? = null

    init{
        view.setOnClickListener{ view ->
            navigateFunction(currentFolder!!.folderId)
        }
    }

    fun bind(folder: Folder){
        currentFolder = folder
        tvFolderTitle.text = folder.name
        //ivFolder.setImageResource(R.drawable.folder)
        ivFolder.setImageResource(currentFolder!!.color)
    }

}