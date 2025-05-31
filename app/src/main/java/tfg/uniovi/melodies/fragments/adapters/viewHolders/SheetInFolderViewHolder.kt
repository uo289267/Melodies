package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto

class SheetInFolderViewHolder(
    private val view: View,
    private val navigateFunction: (SheetVisualizationDto) -> Unit
    ): RecyclerView.ViewHolder(view){
        private var tvSheetTitle : TextView = view.findViewById(R.id.tv_title)
        private var tvSheetAuthor: TextView = view.findViewById(R.id.tv_author)
        private var currentSheet : MusicXMLSheet? = null

    init{
        view.setOnClickListener{
            navigateFunction(SheetVisualizationDto(currentSheet!!.id, currentSheet!!.folderId))
        }
    }
    fun bind(sheet: MusicXMLSheet){
        currentSheet = sheet
        tvSheetAuthor.text = sheet.author
        tvSheetTitle.text = sheet.name
    }
}