package tfg.uniovi.melodies.fragments.adapters.touchHelpers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
/**
 * Custom [ItemTouchHelper.SimpleCallback] to handle swipe gestures on RecyclerView items.
 *
 * This helper enables swipe-to-dismiss (or custom actions) for items in a RecyclerView.
 * Only swipe gestures (start and end) are enabled; drag & drop is disabled.
 *
 * @param onSwipedAction Lambda invoked when an item is swiped.
 *                         - `position` is the adapter position of the swiped item.
 *                         - `direction` indicates the swipe direction (START or END).
 */
class MyItemTouchHelper(
    private val onSwipedAction: (position: Int, direction: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwipedAction(viewHolder.adapterPosition, direction)
    }
}
