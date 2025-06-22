package tfg.uniovi.melodies.utils

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import tfg.uniovi.melodies.R

object ShowAlertDialog {
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
    }
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