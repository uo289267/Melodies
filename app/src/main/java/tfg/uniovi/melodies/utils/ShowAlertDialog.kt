package tfg.uniovi.melodies.utils

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import tfg.uniovi.melodies.R
import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.fragments.viewmodels.ProfileViewModel
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import tfg.uniovi.melodies.repositories.UsersFirestore

/**
 * Static class that allows to easily create and show [AlertDialog]
 */
object ShowAlertDialog {
    private const val RENAME: String = "RENAME"
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
     * @param actionPositive action for when the positive button is pressed.
     */

    fun showAlertDialogOnlyWithPositiveNNegativeButton(context: Context,
                                                       title: String,
                                                       message: String,
                                                       tagForLog: String,
                                                       msgForPositiveBtnLog:
                                                       String,msgForNegativeBtnLog: String,
                                                       actionPositive: ()-> Unit = {},
                                                       actionNegative: () -> Unit ={}){
        AlertDialog.Builder(context).setTitle(title)
            .setMessage(
                message
            )
            .setIcon(R.drawable.icon_alert)
            .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                actionPositive()
                Log.d(tagForLog, msgForPositiveBtnLog)
            }.setNegativeButton(android.R.string.cancel){ dialogInterface, i ->
                actionNegative()
                Log.d(tagForLog, msgForNegativeBtnLog)
            }.show()
    }
    /**
     * Shows a dialog to rename a music sheet.
     *
     * It performs local validations and checks with Firestore if the sheet name
     * is already in use within the same folder.
     *
     * @param context The Context used to show the dialog.
     * @param lifecycleOwner The LifecycleOwner used to launch coroutines.
     * @param sheetDB The database interface for folders and sheets.
     * @param currentSheet The MusicXMLSheet object being renamed.
     * @param titleRes The resource ID for the dialog title.
     * @param messageRes The resource ID for the dialog message.
     * @param optionalButtonText Optional text for a neutral button.
     * @param onOptionalButtonClick Optional callback when the neutral button is clicked.
     * @param onConfirm Callback invoked with the new name if the rename is confirmed.
     */
    fun showRenameSheetDialog(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        sheetDB: FoldersAndSheetsFirestore,
        currentSheet: MusicXMLSheet,
        titleRes: String,
        messageRes: String,
        optionalButtonText: String? = null,
        onOptionalButtonClick: (() -> Unit)? = null,
        onConfirm: (String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.input_dialog, null)
        val input = dialogView.findViewById<EditText>(R.id.input)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress)

        // Prellenar con nombre actual si existe
        input.setText(currentSheet.name)
        input.setSelection(currentSheet.name.length)

        val builder = AlertDialog.Builder(context)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        if (optionalButtonText != null && onOptionalButtonClick != null) {
            builder.setNeutralButton(optionalButtonText) { _, _ -> onOptionalButtonClick() }
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val newName = input.text.toString().trim()
                if (newName == currentSheet.name) {
                    dialog.dismiss()
                    return@setOnClickListener
                }
                // Validaciones locales
                if (newName.isEmpty()) {
                    input.error = context.getString(R.string.rename_nick_empty_err)
                    return@setOnClickListener
                }
                if (newName.length > 20) {
                    input.error = context.getString(R.string.rename_nick_length_err)
                    return@setOnClickListener
                }

                // Bloquear botón y mostrar progreso
                button.isEnabled = false
                progressBar.visibility = View.VISIBLE

                lifecycleOwner.lifecycleScope.launch {
                    try {
                            // Comprobación de disponibilidad del nombre de sheet
                            val taken = sheetDB.isSheetNameInUse(newName, currentSheet.folderId)

                            button.isEnabled = true
                            progressBar.visibility = View.GONE

                            if (taken == true) {
                                input.error = context.getString(R.string.sheet_name_taken_err)
                            } else {
                                dialog.dismiss()
                                onConfirm(newName)
                            }

                    } catch (e: Exception) {
                        button.isEnabled = true
                        progressBar.visibility = View.GONE
                        input.error = context.getString(R.string.error_generic)
                        Log.e(RENAME, "Error checking sheet name", e)
                    }
                }
            }
        }

        dialog.show()
    }
    /**
     * Shows a dialog to rename a folder.
     *
     * It performs local validations and checks with Firestore if the folder name
     * is already in use.
     *
     * @param context The Context used to show the dialog.
     * @param lifecycleOwner The LifecycleOwner used to launch coroutines.
     * @param folderDB The database interface for folders and sheets.
     * @param currentFolderName The current folder name to pre-fill the input.
     * @param titleRes The resource ID for the dialog title.
     * @param messageRes The resource ID for the dialog message.
     * @param optionalButtonText Optional text for a neutral button.
     * @param onOptionalButtonClick Optional callback when the neutral button is clicked.
     * @param onConfirm Callback invoked with the new name if the rename is confirmed.
     */
    fun showRenameFolderDialog(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        folderDB: FoldersAndSheetsFirestore,
        currentFolderName: String?,
        titleRes: String,
        messageRes: String,
        optionalButtonText: String? = null,
        onOptionalButtonClick: (() -> Unit)? = null,
        onConfirm: (String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.input_dialog, null)
        val input = dialogView.findViewById<EditText>(R.id.input)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress)

        // Prellenar con nombre actual si existe
        input.setText(currentFolderName)
        input.setSelection(currentFolderName?.length ?: 0)

        val builder = AlertDialog.Builder(context)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        if (optionalButtonText != null && onOptionalButtonClick != null) {
            builder.setNeutralButton(optionalButtonText) { _, _ -> onOptionalButtonClick() }
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val newName = input.text.toString().trim()
                if (newName == currentFolderName) {
                    dialog.dismiss()
                    return@setOnClickListener
                }
                // Validaciones locales
                if (newName.isEmpty()) {
                    input.error = context.getString(R.string.rename_nick_empty_err)
                    return@setOnClickListener
                }
                if (newName.length > 30) {
                    input.error = context.getString(R.string.rename_nick_length_err)
                    return@setOnClickListener
                }

                // Bloquear botón y mostrar progreso
                button.isEnabled = false
                progressBar.visibility = View.VISIBLE

                lifecycleOwner.lifecycleScope.launch {
                    try {
                        // Comprobación de disponibilidad del nombre de carpeta
                        val taken = folderDB.isFolderNameInUse(newName) // suspend function

                        button.isEnabled = true
                        progressBar.visibility = View.GONE

                        if (taken == true) {
                            input.error =  context.getString(R.string.folder_name_taken_err)
                        } else {
                            dialog.dismiss()
                            onConfirm(newName)
                        }
                    } catch (e: Exception) {
                        button.isEnabled = true
                        progressBar.visibility = View.GONE
                        input.error =  context.getString(R.string.error_generic)
                        Log.e(RENAME, "Error checking folder name", e)
                    }
                }
            }
        }

        dialog.show()
    }
    /**
     * Shows a dialog to input a new nickname for the user.
     *
     * It performs validations and checks with Firestore if the nickname
     * is already taken.
     *
     * @param context The Context used to show the dialog.
     * @param lifecycleOwner The LifecycleOwner used to launch coroutines.
     * @param usersBD The database interface for user data.
     * @param currentNickname The current nickname to pre-fill the input.
     * @param titleRes The resource ID for the dialog title.
     * @param messageRes The resource ID for the dialog message.
     * @param validations A list of validation pairs, each containing a check function and an error message.
     * @param optionalButtonText Optional text for a neutral button.
     * @param onOptionalButtonClick Optional callback when the neutral button is clicked.
     * @param onConfirm Callback invoked with the new nickname if the input is confirmed.
     */
    fun showInputNewNicknameDialog(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        usersBD: UsersFirestore,
        currentNickname: String,
        titleRes: String,
        messageRes: String,
        validations: List<Pair<(String) -> Boolean, String>> = emptyList(),
        optionalButtonText: String? = null,
        onOptionalButtonClick: (() -> Unit)? = null,
        onConfirm: (String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.input_dialog, null)
        val input = dialogView.findViewById<EditText>(R.id.input)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress)

        input.setText(currentNickname)
        input.setSelection(currentNickname.length)

        val builder = AlertDialog.Builder(context)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            .setIcon(R.drawable.icon_alert)

        if (optionalButtonText != null && onOptionalButtonClick != null) {
            builder.setNeutralButton(optionalButtonText) { _, _ -> onOptionalButtonClick() }
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val nickname = input.text.toString().trim()

                // Si no cambió el nombre, dismiss directamente
                if (nickname == currentNickname) {
                    dialog.dismiss()
                    return@setOnClickListener
                }

                val failed = validations.firstOrNull { (check, _) -> !check(nickname) }
                if (failed != null) {
                    input.error = failed.second
                    return@setOnClickListener
                }

                button.isEnabled = false
                progressBar.visibility = View.VISIBLE

                lifecycleOwner.lifecycleScope.launch {
                    try {
                        val taken = usersBD.nicknameExists(nickname)

                        button.isEnabled = true
                        progressBar.visibility = View.GONE

                        if (taken) {
                            input.error = context.getString(R.string.nickname_unable)
                        } else {
                            dialog.dismiss()
                            onConfirm(nickname)
                        }
                    } catch (e: Exception) {
                        button.isEnabled = true
                        progressBar.visibility = View.GONE
                        input.error = context.getString(R.string.nickname_unable)
                        Log.e(RENAME, "Error checking nickname", e)
                    }
                }
            }
        }

        dialog.show()
    }


}