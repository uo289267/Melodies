package tfg.uniovi.melodies.folderManagement.rename
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.scrollTo
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
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
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
class RenameFolderEmptyTest {
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
    fun renameFolderEmptyTest() {
        val originalFolderName = "Basic Sheets"
        // Step 2: Long click the folder to rename (in Home)
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(originalFolderName)), longClick()
                )
            )

        // Wait for rename dialog
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 2000)

        // Clear text to make it empty
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "input"))
        inputField?.text = ""

        // Click OK
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()

        // Check that error message is displayed in dialog
        onView(withId(R.id.input))
            .check(matches(hasErrorText("Folder's name cannot be empty")))

        // Cancel rename dialog
        val cancelButton = onView(
            allOf(
                withId(android.R.id.button2), withText("Cancel"),
                childAtPosition(
                    childAtPosition(withId(androidx.appcompat.R.id.buttonPanel), 0),
                    2
                )
            )
        )
        cancelButton.perform(scrollTo(), click())

        // Verify folder name in Home is still the original
        onView(withText(originalFolderName))
            .check(matches(isDisplayed()))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup &&
                        parentMatcher.matches(parent) &&
                        view == parent.getChildAt(position)
            }
        }
    }
}