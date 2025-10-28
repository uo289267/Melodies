package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.model.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto
import tfg.uniovi.melodies.fragments.utils.ShowAlertDialog

private const val SHEET_RENAME = "SHEET_RENAME"

class SheetInFolderViewHolder(
    private val view: View,
    private val navigateFunction: (SheetVisualizationDto) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: LibraryViewModel
    ): RecyclerView.ViewHolder(view){
        private var tvSheetTitle : TextView = view.findViewById(R.id.tv_title)
        private var tvSheetAuthor: TextView = view.findViewById(R.id.tv_author)
        private var currentSheet : MusicXMLSheet? = null
        private var btnPlay : ImageButton = view.findViewById(R.id.btn_start_playing)

    init{
        view.setOnClickListener{
            navigateFunction(SheetVisualizationDto(currentSheet!!.id, currentSheet!!.folderId))
        }
        btnPlay.setOnClickListener {
            navigateFunction(SheetVisualizationDto(currentSheet!!.id, currentSheet!!.folderId))
        }

        itemView.setOnLongClickListener {
            currentSheet?.let { it1 -> showInputDialog(it1) }
            true
        }

    }
    fun bind(sheet: MusicXMLSheet){
        currentSheet = sheet
        tvSheetAuthor.text = sheet.author
        tvSheetTitle.text = sheet.name
    }


    private fun showInputDialog(sheet: MusicXMLSheet) {
        ShowAlertDialog.showRenameSheetDialog(
            context = view.context,
            lifecycleOwner = lifecycleOwner,
            viewModel = viewModel,
            currentSheet = sheet,
            titleRes = view.context.getString(R.string.rename) + " " + sheet.name,
            messageRes = view.context.getString(R.string.rename_quest),
            optionalButtonText = view.context.getString(R.string.delete_btn),
            onOptionalButtonClick = {
                currentSheet?.let {
                    viewModel.deleteSheet(it.id, it.folderId)
                }
                Toast.makeText(
                    view.context,
                    sheet.name + " " + view.context.getString(R.string.delete_successful),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(DELETE, "Sheet ${currentSheet?.name} was deleted")
            }
        ) { newName ->
            Log.d(SHEET_RENAME, "Renaming sheet to: $newName")
            viewModel.renameSheet(currentSheet!!.id, currentSheet!!.folderId,newName)
            viewModel.loadSheets()
        }


    }


}