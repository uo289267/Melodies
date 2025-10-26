package tfg.uniovi.melodies.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.model.Colors

class SpinnerFoldersColorsAdapter (context: Context, private val folderColors : List<Triple<Int,Int, Colors>>):
    ArrayAdapter<Triple<Int, Int, Colors>>(context,0,folderColors) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createFolderColorView(position, parent)
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createFolderColorView(position, parent)
    }

    private fun createFolderColorView(position: Int, parent: ViewGroup): View {
        //folder icon
        val color = folderColors[position].first
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_folder_color_item, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.iv_folder_with_color)
        imageView.setImageDrawable(ContextCompat.getDrawable(context, color))
        //text name
        val textView = view.findViewById<TextView>(R.id.tv_color_name)
        textView.text = getString(parent.context, folderColors[position].second)
        return view
    }

}