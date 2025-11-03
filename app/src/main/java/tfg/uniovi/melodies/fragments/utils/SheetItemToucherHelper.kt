package tfg.uniovi.melodies.fragments.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
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
        context: Context,
    ): ItemTouchHelper {
        return ItemTouchHelper(
            MyItemTouchHelper { position, direction ->
                if (direction == ItemTouchHelper.START || direction == ItemTouchHelper.END) {
                    val nameOfSheet = adapter.getNameOfSheetAtPosition(position)
                    ShowAlertDialog.showAlertDialogOnlyWithPositiveNNegativeButton(
                        context = context,
                        title = ContextCompat.getString(
                            context,
                            R.string.delete_title
                        ) + " " + nameOfSheet,
                        message = ContextCompat.getString(context, R.string.delete_sheet_quest),
                        tagForLog = DELETE,
                        msgForPositiveBtnLog = "Deleting sheet at $position position",
                        msgForNegativeBtnLog = "Not deleting sheet at $position position",
                        actionPositive = {
                            adapter.removeItemAt(position)
                            viewModel.deleteSheetAt(position)
                            Log.d(DELETE, "One sheet at position $position was deleted")
                            Toast.makeText(
                                context,
                                nameOfSheet + " " + context.getString(R.string.delete_successful),
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