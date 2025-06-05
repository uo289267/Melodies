package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.adapters.viewHolders.SheetInFolderViewHolder
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationDto
import tfg.uniovi.melodies.utils.SheetDiffCallback

class SheetInFolderAdapter : RecyclerView.Adapter<SheetInFolderViewHolder> {
    private val sheetList: MutableList<MusicXMLSheet>
    private val navigateFunction: (SheetVisualizationDto) -> Unit
    private val viewModel : LibraryViewModel

    constructor(
        sheetList: List<MusicXMLSheet>,
        navigateFunction: (SheetVisualizationDto) -> Unit,
        viewModel: LibraryViewModel){
        this.sheetList = sheetList.toMutableList()
        this.navigateFunction = navigateFunction
        this.viewModel = viewModel
    }

    fun updateSheets(newSheets: List<MusicXMLSheet>){
        val oldSize = sheetList.size
        sheetList.clear()
        sheetList.addAll(newSheets)
        //items beyond the newsize need to be removed when the oldsize is greater than the newsize
        if(oldSize > sheetList.size)
            notifyItemRangeRemoved(sheetList.size, oldSize - sheetList.size)
        //all items need to be updated
        notifyItemRangeChanged(0, sheetList.size)
        /*
        val diffCallback = SheetDiffCallback(sheetList, newSheets)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        sheetList.clear()
        sheetList.addAll(newSheets)
        diffResult.dispatchUpdatesTo(this)*/

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SheetInFolderViewHolder {
        val layout = R.layout.recycler_song_in_library_item
        val view = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return SheetInFolderViewHolder(view, navigateFunction)
    }

    override fun onBindViewHolder(viewHolder: SheetInFolderViewHolder, position: Int) {
        viewHolder.bind(sheetList[position])
    }

    override fun getItemCount(): Int {
        return sheetList.size
    }

    fun removeItemAt(position: Int) {
        sheetList.removeAt(position)
        notifyItemRemoved(position)
    }

}