package tfg.uniovi.melodies.fragments.adapters.viewHolders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.MusicXMLSheet

class SheetInFolderViewHolder(
    private val view: View,
    private val navigateFunction: (String) -> Unit
    ): RecyclerView.ViewHolder(view){
        private var tvSheetTitle : TextView = view.findViewById(R.id.tv_title)
        private var tvSheetAuthor: TextView = view.findViewById(R.id.tv_author)
        private var currentSheet : MusicXMLSheet? = null

    init{
        view.setOnClickListener{
            navigateFunction(currentSheet!!.id)
        }
    }
    fun bind(sheet: MusicXMLSheet){
        currentSheet = sheet
        tvSheetAuthor.text = sheet.author
        tvSheetTitle.text = sheet.name
    }
}