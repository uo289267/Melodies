package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import tfg.uniovi.melodies.utils.ShowAlertDialog
private const val FOLDER_RENAME = "FOLDER_RENAME"
class FolderViewHolder(
    private val view: View,
    private val navigateFunction: (String) -> Unit,
    private val onLongClickDelete: (String) -> Unit,
    private val onLongClickRename: (String, String) -> Unit,
    private val lifecycleOwner: LifecycleOwner
): RecyclerView.ViewHolder(view) {
    private val tvFolderTitle: TextView = view.findViewById(R.id.tv_folder_title)
    private val ivFolder: ImageView = view.findViewById(R.id.iv_folder)
    private var currentFolder : Folder? = null

    init{
        view.setOnClickListener{
            navigateFunction(currentFolder!!.folderId)
        }
        itemView.setOnLongClickListener {
            showInputDialog()
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

    private fun showInputDialog(){
        ShowAlertDialog.showRenameFolderDialog(
            context = view.context,
            lifecycleOwner = lifecycleOwner,
            folderDB = FoldersAndSheetsFirestore( PreferenceManager.getUserId(view.context)!!), // tu instancia de la BD
            currentFolderName = currentFolder?.name,
            titleRes = view.context.getString(R.string.rename_folder_title),
            messageRes = view.context.getString(R.string.rename_quest),
            optionalButtonText = view.context.getString(R.string.delete_folder),
            onOptionalButtonClick = {
                currentFolder?.let { onLongClickDelete(it.folderId) }
                Toast.makeText(
                    view.context,
                    view.context.getString(R.string.delete_confirm),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("DELETE", "Folder ${currentFolder?.name} was deleted")
            }
        ) { newName ->
            Log.d("FOLDER_RENAME", "Renaming folder to: $newName")
            currentFolder?.let { onLongClickRename(it.folderId, newName) }
        }
        //TODO REMOVE
        /*
                ShowAlertDialog.showInputDialog(
                    view.context,
                    titleRes = view.context.getString(R.string.rename_folder_title),
                    messageRes = getString(view.context, R.string.rename_quest),
                    optionalButtonText = view.context.getString(R.string.delete_folder),
                    onOptionalButtonClick = {
                        currentFolder?.let {
                            onLongClickDelete(it.folderId)
                        }
                        Toast.makeText(
                            view.context,
                            view.context.getString(R.string.delete_confirm),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(DELETE, "Folder ${currentFolder?.name} was deleted")
                    }
                ){ newName ->
                    Log.d(FOLDER_RENAME, "Renaming sheet to: $newName")
                    currentFolder?.let {
                        onLongClickRename(
                            it.folderId,
                            newName
                        )
                    }
                }*/
    }

}