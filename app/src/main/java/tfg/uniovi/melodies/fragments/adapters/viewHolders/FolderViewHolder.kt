package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.model.Colors
import tfg.uniovi.melodies.model.Folder
import tfg.uniovi.melodies.fragments.viewmodels.FolderViewModel
import tfg.uniovi.melodies.utils.ShowAlertDialog

private const val FOLDER_RENAME = "FOLDER_RENAME"
class FolderViewHolder(
    private val view: View,
    private val navigateFunction: (String) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
    private val folderViewModel: FolderViewModel
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
            currentFolderName = currentFolder?.name,
            titleRes = view.context.getString(R.string.rename_folder_title),
            messageRes = view.context.getString(R.string.rename_quest),
            optionalButtonText = view.context.getString(R.string.delete_folder),
            viewModel = folderViewModel,
            onOptionalButtonClick = {
                currentFolder?.let { folderViewModel.deleteFolder(it.folderId) }
                Toast.makeText(
                    view.context,
                    view.context.getString(R.string.delete_confirm),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(DELETE, "Folder ${currentFolder?.name} was deleted")
            }
        ) { newName ->
            Log.d(FOLDER_RENAME, "Renaming folder to: $newName")
            currentFolder?.let {  folderViewModel.renameFolder(it.folderId, newName) }
        }
    }

}