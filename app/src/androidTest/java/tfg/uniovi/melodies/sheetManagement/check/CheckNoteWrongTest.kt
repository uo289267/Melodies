package tfg.uniovi.melodies.sheetManagement.check

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModel
import tfg.uniovi.melodies.utils.TestUtils

class CheckNoteWrongTest {

    private lateinit var device: UiDevice
    @get:Rule
    val mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)


    @get:Rule
    val grantPermissionRule = GrantPermissionRule.grant("android.permission.RECORD_AUDIO")

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("check2")
    }

    @After
    fun tearDown() {
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
        TestUtils.cleanupTestUser("check2")
    }

    @Test
    fun checkNoteWrongTest() {
        // Wait for recycler
        device.wait(Until.hasObject(By.res("tfg.uniovi.melodies", "recyclerView")), 5000)
        device.wait(Until.hasObject(By.textContains("Estrellita Dónde Estás")), 7000)

        //  Open Basic Sheets
        onView(allOf(withId(R.id.recyclerView), childAtPosition(withId(R.id.constraint), 0)))
            .perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Wait for Estrellita Donde Estas
        device.wait(Until.hasObject(By.textContains("Estrellita Dónde Estás")), 7000)

        // Click on Estrellita Donde Estas
        onView(withId(R.id.recycler_view_library))
            .perform(
                androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem<ViewHolder>(
                    hasDescendant(withText("Estrellita Dónde Estás")),
                    click()
                )
            )


        device.wait(Until.hasObject(By.res("tfg.uniovi.melodies", "sheetImageView")), 7000)
        device.waitForIdle()

        // Check that content is showing
        onView(
            allOf(
                withId(R.id.sheetImageView),
                withParent(withParent(withId(R.id.fragmentContainerView))),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))
        // Get ViewModel to check svg value
        mActivityScenarioRule.scenario.onActivity { activity ->
            val navHostFragment =
                activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            val currentFragment =
                navHostFragment?.childFragmentManager?.fragments?.firstOrNull()

            val viewModel = ViewModelProvider(currentFragment!!)[SheetVisualizationViewModel::class.java]
            val svgContent = viewModel.svg.value

            assertNotNull("SVG should not be null", svgContent)
            assertTrue("SVG should contain <svg>", svgContent!!.contains("<svg"))
            assertTrue("SVG should contain paths or groups", svgContent.contains("<path") || svgContent.contains("<g"))
            assertTrue("SVG should contain red when note is correct", svgContent.contains("#ed6b11"))

        }
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
