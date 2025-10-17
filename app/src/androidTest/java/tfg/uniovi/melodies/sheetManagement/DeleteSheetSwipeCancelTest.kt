package tfg.uniovi.melodies.sheetManagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.repositories.UsersFirestore

class DeleteSheetSwipeCancelTest {

    private lateinit var userRepository: UsersFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("delete", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("delete")
            } catch (e: Exception) {
                println("Error deleting test user: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    @Test
    fun cancelDeleteFirstSheetWithSwipeWithoutScroll() {
        // Log in with user "delete"
        onView(withId(R.id.input_nickname)).perform(
            androidx.test.espresso.action.ViewActions.replaceText("delete"),
            androidx.test.espresso.action.ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Navigate to Full Library using bottom navigation
        onView(withId(R.id.fullLibrary)).perform(click())
        Thread.sleep(1000)

        val sheetTitle = "Canción de Cuna"

        // Wait until the sheet appears
        val sheet = device.wait(Until.findObject(By.text(sheetTitle)), 2000)

        if (sheet != null) {
            // Swipe left on the sheet to trigger the delete dialog
            sheet.swipe(Direction.LEFT, 1.0f)

            // Wait for the confirmation dialog
            device.wait(Until.hasObject(By.textContains("Cancel")), 2000)

            // Click "Cancel" instead of confirming deletion
            device.findObject(By.textContains("Cancel"))?.click()

            // Wait briefly for the dialog to close
            Thread.sleep(1000)

            // Check that the sheet is still visible (not deleted)
            val stillThere = device.wait(Until.hasObject(By.text(sheetTitle)), 2000)
            assert(stillThere) { "Sheet '$sheetTitle' should still be visible after canceling deletion" }

        } else {
            throw RuntimeException("Sheet '$sheetTitle' not found in Full Library")
        }
    }



}