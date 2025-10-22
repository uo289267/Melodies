package tfg.uniovi.melodies.sheetManagement.delete

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
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
class DeleteSheetSwipeCancelTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("delete2")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("delete2")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    @Test
    fun deleteSheetSwipeCancelTest() {
        // Navigate to Full Library using bottom navigation
        onView(withId(R.id.fullLibrary)).perform(click())

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

            // Check that the sheet is still visible (not deleted)
            val stillThere = device.wait(Until.hasObject(By.text(sheetTitle)), 3000)
            assert(stillThere) { "Sheet '$sheetTitle' should still be visible after canceling deletion" }

        } else {
            throw RuntimeException("Sheet '$sheetTitle' not found in Full Library")
        }
    }



}