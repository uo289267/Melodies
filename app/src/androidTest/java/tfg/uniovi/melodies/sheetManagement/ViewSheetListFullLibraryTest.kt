package tfg.uniovi.melodies.sheetManagement
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
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
class ViewSheetListFullLibraryTest {
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
    fun createFolderImportSheetAndCheckFullLibrary() {
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

        // Step 4: Enter the newly created folder
        Thread.sleep(2000)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(folderName)), click()
                )
            )

        // Step 5: Import sheet inside the folder
        onView(withId(R.id.fab_import_sheet)).perform(click())
        onView(withId(R.id.imgBtnUploadFile)).perform(click())

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

        device.wait(Until.hasObject(By.textContains("Import")), 3000)
        onView(withId(R.id.btnImport)).perform(click())

        // Step 6: Go to Full Library
        onView(withId(R.id.fullLibrary)).perform(click())
        device.wait(Until.hasObject(By.textContains(folderName)), 5000)

        // Step 8: Check that the sheet exists inside the folder
        device.wait(Until.hasObject(By.textContains(folderName)), 5000)
        device.wait(Until.hasObject(By.textContains("Ejercicio")), 5000)
        onView(withId(R.id.recycler_view_full_library))
            .check(matches(hasDescendant(withText("Ejercicio"))))
        onView(withId(R.id.recycler_view_full_library))
            .check(matches(hasDescendant(withText(folderName))))
    }
}