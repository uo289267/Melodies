package tfg.uniovi.melodies.sheetManagement.rename

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
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
class RenameSheetDeclineTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("rename1")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("rename1")
    }


    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun renameSheetDeclineTest() {
        val originalSheetName = "Canción de Cuna"
        val newSheetName = "Nombre Cancelado"


        // Go to Basic Sheets folder
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Basic Sheets")), click()
                )
            )
        device.wait(Until.hasObject(By.textContains(originalSheetName)), 7000)
        // Long click the sheet to rename
        onView(withId(R.id.recycler_view_library))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(originalSheetName)), longClick()
                )
            )

        // Wait for the rename dialog
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 2000)

        // Enter new name in the EditText
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "input"))
        inputField?.text = newSheetName

        // Click Cancel instead of OK
        val materialButton5 = onView(
            allOf(
                withId(android.R.id.button2), withText("Cancel"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.buttonPanel),
                        0
                    ),
                    2
                )
            )
        )
        materialButton5.perform(scrollTo(), click())

        // Wait until the sheet still shows the original name
        device.wait(Until.hasObject(By.textContains(originalSheetName)), 3000)

        // Verify the sheet still has the original name
        onView(withId(R.id.recycler_view_library))
            .check { view, _ ->
                val recycler = view as RecyclerView
                var foundOriginal = false
                for (i in 0 until recycler.childCount) {
                    val childTextView = recycler.getChildAt(i)
                        .findViewById<android.widget.TextView>(R.id.tv_title)
                    if (childTextView != null && childTextView.text.toString() == originalSheetName) {
                        foundOriginal = true
                        break
                    }
                }
                assert(foundOriginal) { "Sheet name changed even though Cancel was pressed" }
            }
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
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}