package tfg.uniovi.melodies.sheetManagement.delete

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
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
class DeleteSheetFromFullLibraryScreenTest {
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("delete0")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("delete0")
    }


    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun deleteSheetFromFullLibraryTest() {
        // Go to FullLibrary tab via Bottom Navigation
        onView(withId(R.id.fullLibrary)).perform(click())
        device.wait(Until.hasObject(By.textContains("Canción de Cuna")), 7000)
        // Long click on "Canción de Cuna"
        onView(withText("Canción de Cuna")).perform(longClick())

        // Click "Delete sheet" in the dialog
        onView(withText(R.string.delete_btn)).perform(click())

        // Verify that "Canción de Cuna" no longer exists
        device.wait(Until.gone(By.textContains("Canción de Cuna")), 5000)
    }
}
