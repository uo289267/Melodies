package tfg.uniovi.melodies.folderManagement

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import org.hamcrest.CoreMatchers.anything
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
class ViewSheetListLibraryTest {

    private lateinit var userRepository: UsersFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("view", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("view")
            } catch (e: Exception) {
                println("⚠️ Error deleting test user: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun createFolderAndImportSheet() {
        val folderName = "Nueva Carpeta"

        // Step 1: Log In
        onView(withId(R.id.input_nickname)).perform(
            replaceText("view"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Step 2: Click FAB to create new folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())

        // Step 3: Enter folder name
        onView(withId(R.id.folder_name_input))
            .perform(replaceText(folderName), closeSoftKeyboard())
        onView(withId(R.id.btn_create_folder)).perform(click())

        // Step 4: Wait for folder to appear and enter it
        Thread.sleep(2000)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(folderName)), click()
                )
            )

        // Step 5: Click FAB to import new sheet
        onView(withId(R.id.fab_import_sheet)).perform(click())

        // Step 6: Click upload file button
        onView(withId(R.id.imgBtnUploadFile)).perform(click())

        // Step 7: Select rests.xml in device storage
        device.wait(Until.hasObject(By.pkg("com.android.documentsui")), 5000)
        val fileToSelect = device.findObject(By.textContains("rests.xml"))
        if (fileToSelect != null) {
            fileToSelect.click()
        } else {
            val downloads = device.findObject(By.textContains("Downloads"))
            downloads?.click()
            device.wait(Until.findObject(By.textContains("rests.xml")), 3000)
            device.findObject(By.textContains("rests.xml"))?.click()
        }

        // Step 8: Confirm import
        device.wait(Until.hasObject(By.textContains("Import")), 3000)
        onView(withId(R.id.btnImport)).perform(click())

        Thread.sleep(2000)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(folderName)), click()
                )
            )
        // Step 9: Verify sheet imported in the new folder
        Thread.sleep(2000)
        onView(withId(R.id.recycler_view_library))
            .check(matches(hasDescendant(withText("Ejercicio"))))


    }
}
