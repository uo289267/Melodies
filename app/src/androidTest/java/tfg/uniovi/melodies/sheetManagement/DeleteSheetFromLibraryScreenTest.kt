package tfg.uniovi.melodies.sheetManagement

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
class DeleteSheetFromLibraryTest {

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
    fun deleteFirstSheetFromLibrary() {
        // Step 1: Log in with user "delete"
        onView(withId(R.id.input_nickname)).perform(
            androidx.test.espresso.action.ViewActions.replaceText("delete"),
            androidx.test.espresso.action.ViewActions.closeSoftKeyboard()
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

        // Step 3: Long click the sheet named "Canción de Cuna"
        onView(withId(R.id.recycler_view_library))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Canción de Cuna")), longClick()
                )
            )

        // Step 4: Wait for the delete dialog
        device.wait(Until.hasObject(By.textContains("Delete sheet")), 2000)

        // Step 5: Click the "Delete sheet" button
        val deleteButton = device.findObject(By.textContains("Delete sheet"))
        deleteButton?.click()

        Thread.sleep(2000)
        // Step 6: Verify the sheet "Canción de Cuna" no longer exists
        onView(withId(R.id.recycler_view_library))
            .check { view, _ ->
                val recycler = view as RecyclerView
                for (i in 0 until recycler.childCount) {
                    val childTextView = recycler.getChildAt(i).findViewById<android.widget.TextView>(R.id.tv_title)
                    if (childTextView != null) {
                        assert(childTextView.text.toString() != "Canción de Cuna")
                    }
                }
            }
    }
}
