package tfg.uniovi.melodies.sheetManagement.delete

import androidx.recyclerview.widget.RecyclerView
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
class DeleteSheetFromLibraryTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("delete1")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("delete1")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun deleteFirstSheetFromLibraryTest() {
        //Go to Basic Sheets folder
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Basic Sheets")), click()
                )
            )
        val recycler = device.findObject(By.res("tfg.uniovi.melodies", "recycler_view_library"))
        recycler.wait(Until.hasObject(By.text("Canción de Cuna")), 5000)

        // Long click the sheet named "Canción de Cuna"
        onView(withId(R.id.recycler_view_library))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Canción de Cuna")), longClick()
                )
            )

        // Wait for the delete dialog
        device.wait(Until.hasObject(By.textContains("Delete sheet")), 2000)

        // Click the "Delete sheet" button
        val deleteButton = device.findObject(By.textContains("Delete sheet"))
        deleteButton?.click()

        device.wait(Until.gone(By.textContains("Canción de Cuna")), 5000)
    }
}
