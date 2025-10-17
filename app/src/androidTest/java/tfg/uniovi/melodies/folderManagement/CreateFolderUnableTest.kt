package tfg.uniovi.melodies.folderManagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.repositories.UsersFirestore

class CreateFolderUnableTest {
    private lateinit var userRepository: UsersFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("create", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("create")
            } catch (e: Exception) {
                println("Error deleting test user: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun createFolderAlreadyInUseNameShowsError() {
        // Step 1: Log in with user "create"
        onView(withId(R.id.input_nickname)).perform(
            replaceText("create"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Step 2: Click FAB to add new folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())

        // Step 3: Leave folder name empty
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "folder_name_input"))
        inputField?.text = "Basic Sheets"
        // Step 4: Click create button
        val createButton = device.findObject(By.res("tfg.uniovi.melodies", "btn_create_folder"))
        createButton?.click()
        // Step 5: Wait briefly for error message to appear
        Thread.sleep(1500)

        // Step 6: Verify that the error message is shown (text match only)
        onView(withText(containsString("Folder name already exists, pick a different folder name")))
            .check(matches(isDisplayed()))
    }
}