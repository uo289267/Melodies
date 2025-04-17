package tfg.uniovi.melodies.utils

import androidx.recyclerview.widget.DiffUtil
import tfg.uniovi.melodies.entities.MusicXMLSheet

class SheetDiffCallback(
    private val oldList: List<MusicXMLSheet>,
    private val newList: List<MusicXMLSheet>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compara por ID (o alguna propiedad única)
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compara por contenido completo
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
