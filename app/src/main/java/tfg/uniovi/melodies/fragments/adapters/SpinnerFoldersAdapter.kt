package tfg.uniovi.melodies.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragments.colors
import tfg.uniovi.melodies.model.Colors
import tfg.uniovi.melodies.model.Folder

private const val MAX_CHARS_SHOWN = 13

private const val ELLIPSIS = "..."
class SpinnerFoldersAdapter(context: Context, var folders: MutableList<Folder>)
    : ArrayAdapter<Folder>(context, R.layout.spinner_folder_item, folders) {

    fun updateFolders(newFolders: List<Folder>) {
        clear()
        addAll(newFolders)
        notifyDataSetChanged()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createTextView(convertView, parent, position)
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createTextView(convertView, parent, position)
    }
    private fun createTextView(
        convertView: View?,
        parent: ViewGroup,
        position: Int
    ): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_folder_item, parent, false)

        val folder = getItem(position)
        val drawableRes = colors.find { it.third == (folder?.color ?: Colors.YELLOW) }?.first
        val imageView = view.findViewById<ImageView>(R.id.iv_folder_with_color_import)
        imageView.setImageDrawable(ContextCompat.getDrawable(context, drawableRes!!))
        val textView = view.findViewById<TextView>(R.id.tv_folder_name)
        textView.text = formatName(folder?.name ?: "")
        return view
    }
    private fun formatName(name: String): String {
        return if (name.length <= MAX_CHARS_SHOWN) {
            name
        } else {
            name.substring(0, MAX_CHARS_SHOWN) + ELLIPSIS
        }
    }



}