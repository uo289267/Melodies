package tfg.uniovi.melodies.application

import android.app.Application
import tfg.uniovi.melodies.utils.ShowAlertDialog

class MelodiesApp : Application(){
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            ShowAlertDialog.showAlertDialogOnlyWithPositiveButton(
                context =  baseContext,
                title= "Unexpected Error",
                message = "An unexpected error occurred",
                tagForLog = "EXCEPTION",
                msgForLog = "Exception occurred ${exception.message}"
            )
        }
    }
}