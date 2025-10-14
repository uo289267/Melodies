package tfg.uniovi.melodies

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tfg.uniovi.melodies.repositories.UsersFirestore

@LargeTest
@RunWith(AndroidJUnit4::class)
class ImportSheetWithUiAutomatorTest {
/*rests should be in the devices storage*/
    private lateinit var userRepository: UsersFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("import", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("import")
            } catch (e: Exception) {
                println("⚠️ Error al eliminar usuario de prueba: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun importFileFromDeviceStorage() {
        // LogIn with Import
        onView(withId(R.id.input_nickname)).perform(
            androidx.test.espresso.action.ViewActions.replaceText("import"),
            androidx.test.espresso.action.ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())

        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Go to Import Screen with Bottom Nav
        onView(withId(R.id.importing)).perform(click())

        // Click import button
        onView(withId(R.id.imgBtnUploadFile)).perform(click())

        // Interact with devices storage to select rests.xml
        device.wait(Until.hasObject(By.pkg("com.android.documentsui")), 3000)

        val fileToSelect = device.findObject(By.textContains("rests.xml"))
        if (fileToSelect != null) {
            fileToSelect.click()
        } else {
            val downloads = device.findObject(By.textContains("Downloads"))
            downloads?.click()
            device.wait(Until.findObject(By.textContains("rests.xml")), 2000)
            device.findObject(By.textContains("rests.xml"))?.click()
        }

        // Click import
        device.wait(Until.hasObject(By.textContains("Import")), 2000)
        onView(withId(R.id.btnImport)).perform(click())


        // Wait to see Basic Sheets
        device.wait(Until.hasObject(By.textContains("Basic Sheets")), 5000)

        // Click on Folder Basic Sheets
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Basic Sheets")), click()
                )
            )

        // Check "Ejercicio" is on the list
        device.wait(Until.hasObject(By.textContains("Ejercicio")), 5000)
        onView(withId(R.id.recycler_view_library))
            .check(matches(hasDescendant(withText("Ejercicio"))))
    }
}
