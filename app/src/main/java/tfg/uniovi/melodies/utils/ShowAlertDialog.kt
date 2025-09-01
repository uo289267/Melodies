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
    /**
     * Shows an input dialog with a text field, OK, and Cancel buttons.
     *
     * - The input field requests focus automatically and shows the soft keyboard.
     * - If the user presses OK, the input text is returned via [onResult].
     * - If the user presses Cancel, the dialog is dismissed and `null` is returned.
     *
     * @param context The context used to create the dialog.
     * @param title The title of the dialog.
     * @param message The message body of the dialog.
     * @param tagForLog The tag used in the log statement.
     * @param msgForLog The log message written when OK is pressed, followed by the entered text.
     * @param onResult A callback invoked with the entered text, or `null` if canceled.
     */
    fun showInputDialog(
        context: Context,
        title: String,
        message: String,
        tagForLog: String,
        msgForLog: String,
        onResult: (String?) -> Unit
    ) {
        val input = EditText(context).apply {
            hint = "Nuevo Nombre de Folder"
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine()
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setView(input)
            .setIcon(R.drawable.icon_alert)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val text = input.text.toString()
                Log.d(tagForLog, "$msgForLog: $text")
                onResult(text)
            }
            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onResult(null)
            }
            .create()

        input.requestFocus()
        dialog.setOnShowListener {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }

}