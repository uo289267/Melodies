package tfg.uniovi.melodies.sheetManagement.view


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
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
class ViewSheetsContentFromFullLibraryTest {
    private lateinit var device: UiDevice
    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.RECORD_AUDIO"
        )
    @Before
    fun setUp(){
        TestUtils.loginAs("lucia")
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    @After
    fun cleanUp(){
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
        TestUtils.performLogoutIfPossible()
    }
    @Test
    fun viewSheetsContentFromFullLibraryTest() {
        // Ir a la librería completa
        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.fullLibrary), withContentDescription("Library"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottomNavigationView),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        // Wait for "Estrellita Dónde Estás"
        device.wait(Until.hasObject(By.textContains("Estrellita Dónde Estás")), 7000)

        // Click sobre el item que contiene el texto "Estrellita Dónde Estás"
        onView(withId(R.id.recycler_sheets_in_folder))
            .perform(
                RecyclerViewActions.actionOnItem<ViewHolder>(
                    hasDescendant(withText("Estrellita Dónde Estás")),
                    click()
                )
            )
        device.wait(Until.hasObject(By.textContains("Estrellita Dónde Estás")), 7000)

        // Verify sheet's name
        val textView = onView(
            allOf(
                withText("Estrellita Dónde Estás"),
                withParent(
                    allOf(
                        withId(R.id.toolbar),
                        withParent(IsInstanceOf.instanceOf(ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Estrellita Dónde Estás")))
        device.wait(Until.hasObject(By.res("tfg.uniovi.melodies", "sheetImageView")), 7000)
        // Verificación de la imagen de la sheet
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
