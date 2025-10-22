package tfg.uniovi.melodies.sheetManagement.importing

import androidx.recyclerview.widget.RecyclerView
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.utils.TestUtils

@LargeTest
@RunWith(AndroidJUnit4::class)
class ImportSheetFromLibraryScreenTest {
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("import2")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("import2")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun importSheetFromLibraryScreenTest() {
        // View the library of the folder "Basic Sheets"
        device.wait(Until.hasObject(By.textContains("Basic Sheets")), 5000)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Basic Sheets")), click()
                )
            )

        // Click on FAB
        onView(withId(R.id.fab_import_sheet)).perform(click())
        onView(withId(R.id.imgBtnUploadFile)).perform(click())
        // interact with devices storage to import rests.xml
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

        // Click on import
        device.wait(Until.hasObject(By.textContains("Import")), 4000)
        onView(withId(R.id.btnImport)).perform(click())
        onView(withId(R.id.home_fragment)).perform(click())

        // Go back to Basic Sheets to see that the Ejercicio has been imported

        device.wait(Until.hasObject(By.textContains("Basic Sheets")), 5000)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Basic Sheets")), click()
                )
            )

        device.wait(Until.hasObject(By.textContains("Ejercicio")), 6000)
        onView(withId(R.id.recycler_view_library))
            .check(matches(hasDescendant(withText("Ejercicio"))))
    }
}