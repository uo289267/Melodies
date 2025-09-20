package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
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
    private val onLongClickRename: (SheetVisualizationDto, String) -> Unit
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
            showInputDialog()

            true
        }

    }
    fun bind(sheet: MusicXMLSheet){
        currentSheet = sheet
        tvSheetAuthor.text = sheet.author
        tvSheetTitle.text = sheet.name
    }

    private fun showInputDialog() {
        ShowAlertDialog.showInputDialog(
            context = view.context,
            titleRes = getString(view.context, R.string.rename),
            messageRes = getString(view.context, R.string.rename_quest),
            validations = listOf(
                Pair({ it.isNotEmpty() }, getString(view.context, R.string.rename_empty_err)),
                Pair({ it.length <= 20 }, getString(view.context, R.string.rename_length_err))
            )

        ) { newName ->
            Log.d(SHEET_RENAME, "Renaming sheet to: $newName")
            onLongClickRename(
                SheetVisualizationDto(currentSheet!!.id, currentSheet!!.folderId),
                newName
            )
        }
    }


}