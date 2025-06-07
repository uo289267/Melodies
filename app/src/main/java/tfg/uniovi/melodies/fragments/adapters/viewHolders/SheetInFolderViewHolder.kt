package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto
import tfg.uniovi.melodies.utils.ShowAlertDialog

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
            ShowAlertDialog.showInputDialog(
                context = view.context,
                title = "Rename Sheet",
                message = "Enter new name:",
                tagForLog = "SHEET_RENAME",
                msgForLog = "Folder renamed"
            ){
                result ->
                if(result!= null){
                    onLongClickRename(SheetVisualizationDto(currentSheet!!.id, currentSheet!!.folderId), result)
                }
            }
            true
        }

    }
    fun bind(sheet: MusicXMLSheet){
        currentSheet = sheet
        tvSheetAuthor.text = sheet.author
        tvSheetTitle.text = sheet.name
    }
}