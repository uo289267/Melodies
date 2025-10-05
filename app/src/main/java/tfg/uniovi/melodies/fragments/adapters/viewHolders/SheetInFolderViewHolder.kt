package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto
import tfg.uniovi.melodies.utils.ShowAlertDialog

private const val SHEET_RENAME = "SHEET_RENAME"

class SheetInFolderViewHolder(
    private val view: View,
    private val navigateFunction: (SheetVisualizationDto) -> Unit,
    private val onLongClickRename: (SheetVisualizationDto, String) -> Unit,
    private val onDelete: (SheetVisualizationDto) -> Unit
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
        /*
        ShowAlertDialog.showInputDialog(
            view.context,
            titleRes = getString(view.context, R.string.rename)+ " " +sheet.name,
            messageRes = getString(view.context, R.string.rename_quest),
            optionalButtonText = getString(view.context, R.string.delete_btn),
            onOptionalButtonClick = {
                currentSheet?.let {
                    onDelete(SheetVisualizationDto(it.id, it.folderId))
                }
                Toast.makeText(
                    view.context,
                    sheet.name +" "+view.context.getString(R.string.delete_successful),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(DELETE, "sheet ${currentSheet?.name} was deleted")
            }
        ){ newName ->
            Log.d(SHEET_RENAME, "Renaming sheet to: $newName")
            onLongClickRename(
                SheetVisualizationDto(currentSheet!!.id, currentSheet!!.folderId),
                newName
            )
        }*/

    }


}