package tfg.uniovi.melodies.userManagement.nickname


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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

@LargeTest
@RunWith(AndroidJUnit4::class)
class NicknameEmpty {
    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)
    @Before
    fun deleteUser() = runBlocking {
        TestUtils.deleteTestUser("old")
    }
    @After
    fun deleteOldUserIfExists() = runBlocking {
        TestUtils.cleanupTestUser("old")
    }
    @Test
    fun nicknameEmpty() {
        val materialButton = onView(
            allOf(
                withId(R.id.btnRegister), withText("New User? Create account"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.fragmentContainerView),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())

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
        textInputEditText.perform(replaceText("old"), closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.btnRegister), withText("Register"),
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
        materialButton2.perform(click())
    Thread.sleep(2000)
        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.profile), withContentDescription("Profile"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottomNavigationView),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val appCompatImageButton = onView(
            allOf(
                withId(R.id.btnEdit), withContentDescription("change nickname"),
                childAtPosition(
                    allOf(
                        withId(R.id.cardUserId),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            1
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatImageButton.perform(click())

        val appCompatEditText = onView(
            allOf(
                withId(R.id.input), withText("old"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.custom),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText(""))

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.input),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.custom),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(closeSoftKeyboard())

        val materialButton3 = onView(
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
        materialButton3.perform(scrollTo(), click())

        val linearLayout = onView(
            allOf(
                withParent(
                    allOf(
                        withId(androidx.appcompat.R.id.custom),
                        withParent(withId(androidx.appcompat.R.id.customPanel))
                    )
                ),
                isDisplayed()
            )
        )
        linearLayout.check(matches(isDisplayed()))
        onView(withId(R.id.input))
            .check(matches(hasErrorText("New nickname cannot be empty")))
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
