package tfg.uniovi.melodies.sheetManagement.view


import android.view.View
import android.view.ViewGroup
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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.utils.TestUtils

/*Espresso Record Test*/
@LargeTest
@RunWith(AndroidJUnit4::class)
class ViewSheetsContentFromLibraryTest {
    private lateinit var device: UiDevice
    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp(){
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    @After
    fun cleanUp(){
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
        TestUtils.performLogoutIfPossible()
    }
    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.RECORD_AUDIO"
        )

    @Test
    fun viewSheetsContentFromLibraryTest() {
        val textInputEditText = onView(
            allOf(
                withId(R.id.input_nickname),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.layout_user_id),
                        0
                    ),
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
                    childAtPosition(
                        withId(R.id.fragmentContainerView),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())
        Thread.sleep(3000)
        val materialButton2 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.buttonPanel),
                        0
                    ),
                    3
                )
            )
        )
        materialButton2.perform(scrollTo(), click())

        val recyclerView = onView(
            allOf(
                withId(R.id.recyclerView),
                childAtPosition(
                    withId(R.id.constraint),
                    0
                )
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        Thread.sleep(3000)
        val recyclerView2 = onView(
            allOf(
                withId(R.id.recycler_view_library),
                childAtPosition(
                    withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                    1
                )
            )
        )
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        Thread.sleep(7000)
        val imageView = onView(
            allOf(
                withId(R.id.sheetImageView),
                withParent(withParent(withId(R.id.fragmentContainerView))),
                isDisplayed()
            )
        )
        imageView.check(matches(isDisplayed()))
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
