package tfg.uniovi.melodies.sheetManagement

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.repositories.UsersFirestore

@LargeTest
@RunWith(AndroidJUnit4::class)
class RenameSheetFromLibraryTest {

    private lateinit var userRepository: UsersFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("rename", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("rename")
            } catch (e: Exception) {
                println("Error deleting test user: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun renameFirstSheetInLibrary() {
        val originalSheetName = "Canción de Cuna"
        val newSheetName = "Canción Renombrada"

        // Step 1: Log in with user "rename"
        onView(withId(R.id.input_nickname)).perform(
            replaceText("rename"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Step 2: Go to Basic Sheets folder
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Basic Sheets")), click()
                )
            )

        // Step 3: Long click the sheet to rename
        onView(withId(R.id.recycler_view_library))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(originalSheetName)), longClick()
                )
            )

        // Step 4: Wait for the rename dialog
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 2000)

        // Step 5: Enter new name in the EditText
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "input"))
        inputField?.text = newSheetName

        // Step 6: Click OK in the dialog
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()

        Thread.sleep(2000)

        // Step 7: Verify the sheet now has the new name
        onView(withId(R.id.recycler_view_library))
            .check { view, _ ->
                val recycler = view as RecyclerView
                var found = false
                for (i in 0 until recycler.childCount) {
                    val childTextView = recycler.getChildAt(i)
                        .findViewById<android.widget.TextView>(R.id.tv_title)
                    if (childTextView != null && childTextView.text.toString() == newSheetName) {
                        found = true
                        break
                    }
                }
                assert(found) { "Sheet was not renamed correctly" }
            }
    }
}
