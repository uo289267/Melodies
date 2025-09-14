package tfg.uniovi.melodies.utils

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import tfg.uniovi.melodies.R

/**
 * Static class that allows to easily create and show [AlertDialog]
 */
object ShowAlertDialog {
    /**
     * Shows an alert dialog with only a positive button.
     *
     * @param context The context used to create the dialog.
     * @param title The title of the dialog.
     * @param message The message body of the dialog.
     * @param tagForLog The tag used in the log statement when the button is pressed.
     * @param msgForLog The log message to be written when the button is pressed.
     * @param action An optional callback executed when the positive button is pressed.
     */
    fun showAlertDialogOnlyWithPositiveButton(context: Context, title: String, message: String,
                                              tagForLog: String, msgForLog: String,
                                              action: ()-> Unit = {}){
        AlertDialog.Builder(context).setTitle(title)
            .setMessage(
                message
            )
            .setIcon(R.drawable.icon_alert)
            .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                Log.d(tagForLog, msgForLog)
                action()
            }.show()
    }/**
     * Shows an alert dialog with both positive (OK) and negative (Cancel) buttons.
     *
     * @param context The context used to create the dialog.
     * @param title The title of the dialog.
     * @param message The message body of the dialog.
     * @param tagForLog The tag used in log statements.
     * @param msgForPositiveBtnLog The log message written when the positive button is pressed.
     * @param msgForNegativeBtnLog The log message written when the negative button is pressed.
     */

    fun showAlertDialogOnlyWithPositiveNNegativeButton(context: Context, title: String, message: String,
                                              tagForLog: String, msgForPositiveBtnLog:
                                                       String,msgForNegativeBtnLog: String ){
        AlertDialog.Builder(context).setTitle(title)
            .setMessage(
                message
            )
            .setIcon(R.drawable.icon_alert)
            .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                Log.d(tagForLog, msgForPositiveBtnLog)
            }.setNegativeButton(android.R.string.cancel){ dialogInterface, i ->
                Log.d(tagForLog, msgForNegativeBtnLog)
            }.show()
    }

}