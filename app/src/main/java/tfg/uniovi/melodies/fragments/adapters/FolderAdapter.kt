package tfg.uniovi.melodies.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.entities.Folder

class FolderAdapter : RecyclerView.Adapter<FolderViewHolder> {
    private val folderList : MutableList<Folder>
    private val navigateFunction: (String) -> Unit

    constructor(folderList: List<Folder>, navigateFunction: (String) -> Unit, lifecycleOwner: LifecycleOwner){
        this.folderList = folderList.toMutableList()
        this.navigateFunction = navigateFunction
        /*
        this.viewModel = viewModel
        this.viewModel.tareas.observe(lifecycleOwner){lista ->
            updateTareas(lista)
        }*/
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FolderViewHolder {
        val layout = R.layout.recycler_folder_item
        val view = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return FolderViewHolder(view, navigateFunction)
    }

    override fun onBindViewHolder(viewHolder: FolderViewHolder, position: Int) {
        viewHolder.bind(folderList[position])
    }
    override fun getItemCount(): Int {
        return folderList.size
    }
}