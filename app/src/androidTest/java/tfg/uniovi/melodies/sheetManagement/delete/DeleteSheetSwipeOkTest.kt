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
import tfg.uniovi.melodies.fragmentUtils.TestUtils

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeleteSheetSwipeOkTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("delete3")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("delete3")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun deleteSheetSwipeOkTest() {
        // Go to Full Library via bottom nav
        onView(withId(R.id.fullLibrary)).perform(click())

        // Use UiDevice to find the sheet by text
        val sheet = device.wait(Until.findObject(By.text("Canción de Cuna")), 2000)

        if (sheet != null) {
            // Swipe left on the sheet
            sheet.swipe(Direction.LEFT, 1.0f)  // Dirección LEFT, 1.0f es la distancia (100%)

            // Wait for delete dialog
            device.wait(Until.hasObject(By.textContains("OK")), 2000)

            // Click Delete sheet
            device.findObject(By.textContains("OK"))?.click()

            // Wait until it disappears
            device.wait(Until.gone(By.text("Canción de Cuna")), 3000)
        } else {
            throw RuntimeException("Sheet 'Canción de Cuna' not found in Full Library")
        }
    }

}
