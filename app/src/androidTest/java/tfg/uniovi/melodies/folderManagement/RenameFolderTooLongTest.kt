package tfg.uniovi.melodies.folderManagement

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
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
import org.hamcrest.Matchers.containsString
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
class RenameFolderTooLongTest {

    private lateinit var userRepository: UsersFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("rename", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("rename")
            } catch (e: Exception) {
                println("Error deleting test user: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun renameFolderTooLongNameShowsError() {
        val folderToRename = "My Songs"
        // Generate a name longer than the allowed max (e.g., 31+ chars)
        val tooLongName = "ThisFolderNameIsDefinitelyWayTooLongToBeValid"

        // Step 1: Log in
        onView(withId(R.id.input_nickname)).perform(replaceText("rename"), closeSoftKeyboard())
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Step 2: Create "My Songs" folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())
        onView(withId(R.id.folder_name_input)).perform(replaceText(folderToRename), closeSoftKeyboard())
        onView(withId(R.id.btn_create_folder)).perform(click())
        Thread.sleep(1500)

        // Step 3: Long click folder "My Songs"
        onView(withId(R.id.recyclerView)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(folderToRename)), longClick()
            )
        )

        // Step 4: Wait for rename dialog
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 2000)

        // Step 5: Enter a too-long name
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "input"))
        inputField?.text = tooLongName

        // Step 6: Confirm rename
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()

        // Step 7: Wait for error message
        Thread.sleep(1000)

        // Step 8: Verify error text "Folder name too long"
        onView(withId(R.id.input))
            .check(matches(hasErrorText(containsString("Folder's name cannot be longer than 30 characters"))))

        // Step 9: Cancel dialog
        val cancelButton = device.findObject(By.textContains("Cancel"))
        cancelButton?.click()

        // Step 10: Verify folder name remains unchanged
        onView(withText(folderToRename)).check(matches(isDisplayed()))
    }
}
