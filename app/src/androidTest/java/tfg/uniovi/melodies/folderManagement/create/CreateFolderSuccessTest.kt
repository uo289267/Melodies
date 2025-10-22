package tfg.uniovi.melodies.folderManagement.create

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
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
class CreateFolderSuccessTest {
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
    fun createFolderSuccessTest() {
        // Click FAB in Home to add new folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())

        // Enter folder name
        val folderName = "Mi Nueva Carpeta"
        onView(withId(R.id.folder_name_input))
            .perform(replaceText(folderName), closeSoftKeyboard())

        // Click create button
        onView(withId(R.id.btn_create_folder)).perform(click())

        // Verify folder appears in Home RecyclerView
        Thread.sleep(2000)
        onView(withId(R.id.recyclerView))
            .check(matches(hasDescendant(withText(folderName))))
    }

}