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
                    ShowAlertDialog.showAlertDialogOnlyWithPositiveNNegativeButton(
                        context = context,
                        title = "Delete Sheet",
                        message = "Are you sure you want to delete this sheet?",
                        tagForLog = DELETE,
                        msgForPositiveBtnLog = "Deleting sheet at $position position",
                        msgForNegativeBtnLog = "Not deleting sheet at $position position",
                        actionPositive = {
                            adapter.removeItemAt(position)
                            viewModel.deleteSheetAt(position)
                            Log.d(DELETE, "One sheet at position $position was deleted")
                            Toast.makeText(
                                context,
                                context.getString(R.string.delete_successful),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        actionNegative = {
                            viewModel.loadSheets()
                        }
                    )
                }
            }
        )
    }
}