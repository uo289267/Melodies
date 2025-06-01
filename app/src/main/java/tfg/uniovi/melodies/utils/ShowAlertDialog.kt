package tfg.uniovi.melodies.utils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import tfg.uniovi.melodies.R

object ShowAlertDialog {
    fun showAlertDialogOnlyWithPositiveButton(context: Context, title: String, message: String,
                                              tagForLog: String, msgForLog: String){
        AlertDialog.Builder(context).setTitle(title)
            .setMessage(
                message
            )
            .setIcon(R.drawable.icon_alert)
            .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                Log.d(tagForLog, msgForLog)
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
}