package tfg.uniovi.melodies.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
private const val  LEFT_MARGIN : Int = 40
/**
 * Custom [RecyclerView.ItemDecoration] that draws a horizontal divider between items.
 *
 * The divider:
 * - Uses a drawable resource.
 * - Leaves equal left and right margins.
 * - Is not drawn after the last item.
 *
 * @param context The context used to load the divider drawable.
 * @param resId The drawable resource ID of the divider (e.g., a shape or line).
 */
class RecyclerViewItemDecoration (context: Context,
                                  resId: Int
) : RecyclerView.ItemDecoration() {

    private var mDivider: Drawable = ContextCompat.getDrawable(context, resId)!!

    /**
     * Draws the divider between each child view of the RecyclerView,
     * except after the last one.
     *
     * @param c The canvas on which to draw the divider.
     * @param parent The RecyclerView this ItemDecoration is applied to.
     * @param state The current RecyclerView state.
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val dividerLeft: Int = LEFT_MARGIN

        val dividerRight: Int = parent.width - LEFT_MARGIN

        for (i in 0 until parent.childCount) {

            if (i != parent.childCount - 1) {
                val child: View = parent.getChildAt(i)

                val params = child.layoutParams as RecyclerView.LayoutParams

                val dividerTop: Int = child.bottom + params.bottomMargin
                val dividerBottom: Int = dividerTop + mDivider.intrinsicHeight

                mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                mDivider.draw(c)
            }
        }
    }
}
