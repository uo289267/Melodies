package tfg.uniovi.melodies.sheetManagement

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModel

class CheckNoteWrongTest {
    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)
    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.RECORD_AUDIO"
        )
    @Test
    fun viewSheetsContentAndCheckSvg() {
        val textInputEditText = onView(
            allOf(
                withId(R.id.input_nickname),
                childAtPosition(
                    childAtPosition(withId(R.id.layout_user_id), 0),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText.perform(replaceText("lucia"), closeSoftKeyboard())

        val materialButton = onView(
            allOf(
                withId(R.id.btnEnterPreviousAccount), withText("Log In"),
                childAtPosition(
                    childAtPosition(withId(R.id.fragmentContainerView), 0),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())
        Thread.sleep(1000)

        val okButton = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(withId(androidx.appcompat.R.id.buttonPanel), 0),
                    3
                )
            )
        )
        okButton.perform(scrollTo(), click())

        // Open Basic Sheets
        val recyclerView = onView(
            allOf(
                withId(R.id.recyclerView),
                childAtPosition(withId(R.id.constraint), 0)
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Select first sheet
        val recyclerView2 = onView(
            allOf(
                withId(R.id.recycler_view_library),
                childAtPosition(withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")), 1)
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        Thread.sleep(7000)

        // Check ImageView
        onView(
            allOf(
                withId(R.id.sheetImageView),
                withParent(withParent(withId(R.id.fragmentContainerView))),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        // Check SVG
        mActivityScenarioRule.scenario.onActivity { activity ->
            val navHostFragment =
                activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            val currentFragment =
                navHostFragment?.childFragmentManager?.fragments?.firstOrNull()

            val viewModel = ViewModelProvider(currentFragment!!)[SheetVisualizationViewModel::class.java]
            val svgContent = viewModel.svg.value

            assertNotNull("SVG should not be null", svgContent)
            assertTrue("SVG should contain svg tag", svgContent!!.contains("<svg"))
            assertTrue("SVG should contain graphic elements", svgContent.contains("<path") || svgContent.contains("<g"))
            assertTrue("SVG should contain red", svgContent.contains("#FF0000"))
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