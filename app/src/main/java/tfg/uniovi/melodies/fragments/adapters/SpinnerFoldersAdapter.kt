package tfg.uniovi.melodies.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder

private const val MAX_CHARS_SHOWN = 13

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
        val textView = view.findViewById<TextView>(R.id.tv_folder_name)
        textView.text = formatName(folder?.name ?: "")
        return view
    }
    private fun formatName(name: String): String {
        return if (name.length <= MAX_CHARS_SHOWN) {
            name
        } else {
            name.substring(0, MAX_CHARS_SHOWN) + "..."
        }
    }



}