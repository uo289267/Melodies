package tfg.uniovi.melodies.folderManagement.create

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragmentUtils.TestUtils

class CreateFolderUnableTest {
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("create")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("create")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun createFolderUnableTest() {

        // Click FAB to add new folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())

        // Write Basic Sheets
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "folder_name_input"))
        inputField?.text = "Basic Sheets"
        // Click create button
        val createButton = device.findObject(By.res("tfg.uniovi.melodies", "btn_create_folder"))
        createButton?.click()
        // Wait briefly for error message to appear
        val errorText = "Folder name already exists"
        device.wait(
            Until.hasObject(By.textContains(errorText)),
            3000
        )

        onView(withText(containsString(errorText)))
            .check(matches(isDisplayed()))
    }
}