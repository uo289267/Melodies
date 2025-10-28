package tfg.uniovi.melodies.folderManagement.rename
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.Matchers.containsString
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
class RenameFolderUnableTest {
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("rename")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("rename")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun renameFolderUnableTest() {
        val folderToRename = "My Songs"
        val existingFolderName = "Basic Sheets"

        // Create "My Songs" folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())
        onView(withId(R.id.folder_name_input)).perform(replaceText(folderToRename), closeSoftKeyboard())
        onView(withId(R.id.btn_create_folder)).perform(click())
        device.wait(Until.hasObject(By.textContains(folderToRename)), 3000)

        // Long click folder "My Songs"
        onView(withId(R.id.recyclerView)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(folderToRename)), longClick()
            )
        )

        // Wait for rename dialog
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 2000)

        // Enter name of an existing folder
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "input"))
        inputField?.text = existingFolderName

        // Confirm rename
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()

        // Wait for error message
        device.wait(Until.hasObject(By.textContains("Folder name already in use")), 2000)

        onView(withId(R.id.input))
            .check(matches(hasErrorText(containsString("Folder name already in use"))))

        // Cancel dialog
        val cancelButton = device.findObject(By.textContains("Cancel"))
        cancelButton?.click()

        // Verify folder name remains unchanged
        onView(withText(folderToRename)).check(matches(isDisplayed()))
    }
}