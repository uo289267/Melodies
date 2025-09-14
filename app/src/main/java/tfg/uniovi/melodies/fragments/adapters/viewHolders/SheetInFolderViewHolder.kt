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
        val input = EditText(view.context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine()
        }

        val dialog = AlertDialog.Builder(view.context)
            .setTitle(getString(view.context, R.string.rename))
            .setMessage(getString(view.context, R.string.rename_quest))
            .setView(input)
            .setIcon(R.drawable.icon_alert)
            .setPositiveButton(android.R.string.ok, null) // we handle later
            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        input.requestFocus()
        dialog.setOnShowListener {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)

            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val text = input.text.toString().trim()
                if (text.isEmpty()) {
                    input.error = getString(view.context, R.string.rename_empty_err)
                }
                else if(text.length > 20){
                    input.error = getString(view.context, R.string.rename_length_err)
                }
                else {
                    input.error = null
                    Log.d(SHEET_RENAME, "Renaming sheet to: $text")
                    onLongClickRename(SheetVisualizationDto(currentSheet!!.id, currentSheet!!.folderId), text)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

}