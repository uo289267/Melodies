package tfg.uniovi.melodies.sheetManagement

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.repositories.UsersFirestore

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeleteSheetFromLibrarySwipeTest {

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
    fun deleteFirstSheetWithSwipeWithoutScroll() {
        // Log in with user "delete"
        onView(withId(R.id.input_nickname)).perform(
            androidx.test.espresso.action.ViewActions.replaceText("delete"),
            androidx.test.espresso.action.ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Go to Full Library via bottom nav
        onView(withId(R.id.fullLibrary)).perform(click())
        Thread.sleep(1000)

        // Use UiDevice to find the sheet by text
        // Esperar a que aparezca el objeto
        val sheet = device.wait(Until.findObject(By.text("Canción de Cuna")), 2000)

        if (sheet != null) {
            // Swipe left on the sheet
            sheet.swipe(Direction.LEFT, 1.0f)  // Dirección LEFT, 1.0f es la distancia (100%)

            // Wait for delete dialog
            device.wait(Until.hasObject(By.textContains("Delete sheet")), 2000)

            // Click Delete sheet
            device.findObject(By.textContains("Delete sheet"))?.click()

            // Wait until it disappears
            device.wait(Until.gone(By.text("Canción de Cuna")), 3000)
        } else {
            throw RuntimeException("Sheet 'Canción de Cuna' not found in Full Library")
        }
    }

}
