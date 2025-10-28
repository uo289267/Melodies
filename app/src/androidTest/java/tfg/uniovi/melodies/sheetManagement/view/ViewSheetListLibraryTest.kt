package tfg.uniovi.melodies.sheetManagement.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragmentUtils.TestUtils

@LargeTest
@RunWith(AndroidJUnit4::class)
class ViewSheetListLibraryTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("view2")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("view2")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun viewSheetListLibraryTest() {
        val folderName = "Nueva Carpeta"

        // Click FAB to create new folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())

        // Enter folder name
        onView(withId(R.id.folder_name_input))
            .perform(replaceText(folderName), closeSoftKeyboard())
        onView(withId(R.id.btn_create_folder)).perform(click())

        // Wait for folder to appear and enter it
        device.wait(Until.hasObject(By.textContains(folderName)), 3000)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(folderName)), click()
                )
            )

        // Click FAB to import new sheet
        onView(withId(R.id.fab_import_sheet)).perform(click())

        // Click upload file button
        onView(withId(R.id.imgBtnUploadFile)).perform(click())

        // Select rests.xml in device storage
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

        // Confirm import
        device.wait(Until.hasObject(By.textContains("Import")), 3000)
        onView(withId(R.id.btnImport)).perform(click())

        device.wait(Until.hasObject(By.textContains(folderName)), 3000)
        device.wait(Until.hasObject(By.res("tfg.uniovi.melodies", "recycler_view_library")), 5000)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(folderName)), click()
                )
            )
        // Verify sheet imported in the new folder
        device.wait(Until.hasObject(By.textContains("Ejercicio")), 3000)
        onView(withId(R.id.recycler_view_library))
            .check(matches(hasDescendant(withText("Ejercicio"))))


    }
}
