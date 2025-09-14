package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.entities.Folder

class FolderViewHolder(
    private val view: View,
    private val navigateFunction: (String) -> Unit,
    private val onLongClickDelete: (Folder) -> Unit
): RecyclerView.ViewHolder(view) {
    private val tvFolderTitle: TextView = view.findViewById(R.id.tv_folder_title)
    private val ivFolder: ImageView = view.findViewById(R.id.iv_folder)
    private var currentFolder : Folder? = null

    init{
        view.setOnClickListener{
            navigateFunction(currentFolder!!.folderId)
        }
        itemView.setOnLongClickListener {
            AlertDialog.Builder(view.context).setTitle(getString(view.context, R.string.delete))
                .setMessage(
                    getString(view.context, R.string.delete_ques)+ " " +currentFolder?.name +"?"
                )
                .setIcon(R.drawable.icon_alert)
                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                    Log.d(DELETE, "Long click was activated for ${currentFolder?.name}")
                    Toast.makeText(view.context, currentFolder?.name + getString(view.context, R.string.delete_confirm)
                        , Toast.LENGTH_SHORT).show()
                    onLongClickDelete(currentFolder!!)
                }.setNegativeButton(android.R.string.cancel){ dialogInterface, i ->
                    Log.d(DELETE, "Long click was canceled for ${currentFolder?.name}")
                }.show()
            true
        }
    }

    fun bind(folder: Folder){
        currentFolder = folder

        tvFolderTitle.text = folder.name

        val resId = when (folder.color) {
            Colors.YELLOW -> R.drawable.folder_yellow
            Colors.PINK-> R.drawable.folder_pink
            Colors.BLUE -> R.drawable.folder_blue
        }
        ivFolder.setImageResource(resId)
    }

}