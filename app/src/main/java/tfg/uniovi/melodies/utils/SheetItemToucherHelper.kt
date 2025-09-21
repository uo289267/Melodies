package tfg.uniovi.melodies.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragments.adapters.SheetInFolderAdapter
import tfg.uniovi.melodies.fragments.adapters.touchHelpers.MyItemTouchHelper
import tfg.uniovi.melodies.fragments.viewmodels.LibraryViewModel

private const val DELETE = "DELETE"

object SheetItemToucherHelper {
    fun create(
        adapter: SheetInFolderAdapter,
        viewModel: LibraryViewModel,
        context: Context
    ): ItemTouchHelper {
        return ItemTouchHelper(
            MyItemTouchHelper { position, direction ->
                if (direction == ItemTouchHelper.START || direction == ItemTouchHelper.END) {
                    // Quitar del adaptador
                    adapter.removeItemAt(position)
                    // Eliminar de la BD a través del ViewModel
                    viewModel.deleteSheetAt(position)
                    // Log y Toast
                    Log.d(DELETE, "One sheet at position $position was deleted")
                    Toast.makeText(
                        context,
                        context.getString(R.string.delete_successful),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}