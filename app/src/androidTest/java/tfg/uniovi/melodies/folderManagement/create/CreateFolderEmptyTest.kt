package tfg.uniovi.melodies.folderManagement.create

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsInstanceOf
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
class CreateFolderEmptyTest {
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
    fun createFolderEmptyTest() {

        // Click FAB to add new folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())

        // Leave folder name empty
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "folder_name_input"))
        inputField?.text = ""

        // Click create button
        val createButton = device.findObject(By.res("tfg.uniovi.melodies", "btn_create_folder"))
        createButton?.click()
        val textView = onView(
            allOf(
                withId(com.google.android.material.R.id.textinput_error),
                withText("Folder's name cannot be empty"),
                withParent(withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java))),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Folder's name cannot be empty")))
    }
}
