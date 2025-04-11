package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.adapters.viewHolders.SheetViewHolder

class SheetAdapter : RecyclerView.Adapter<SheetViewHolder> {
    private val sheetList: MutableList<MusicXMLSheet>
    private val navigateFunction: (String) -> Unit

    constructor(sheetList: List<MusicXMLSheet>, navigateFunction: (String) -> Unit, lifecycleOwner: LifecycleOwner){
        this.sheetList = sheetList.toMutableList()
        this.navigateFunction = navigateFunction
        /*
        this.viewModel = viewModel
        this.viewModel.tareas.observe(lifecycleOwner){lista ->
            updateTareas(lista)
        }*/
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SheetViewHolder {
        val layout = R.layout.recycler_song_in_library_item
        val view = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return SheetViewHolder(view, navigateFunction)
    }

    override fun onBindViewHolder(viewHolder: SheetViewHolder, position: Int) {
        viewHolder.bind(sheetList[position])
    }

    override fun getItemCount(): Int {
        return sheetList.size
    }
}