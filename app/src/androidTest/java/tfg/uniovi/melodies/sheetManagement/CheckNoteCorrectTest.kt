package tfg.uniovi.melodies.sheetManagement

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragments.viewmodels.SheetVisualizationViewModel
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue

@RunWith(AndroidJUnit4::class)
class CheckNoteCorrectTest {

    @get:Rule
    val mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val mGrantPermissionRule = GrantPermissionRule.grant(
        "android.permission.RECORD_AUDIO"
    )

    @Before
    fun setup() {
        // Mockeamos el objeto PitchDetector antes de lanzar la actividad
        mockkObject(PitchDetector)
        every { PitchDetector.startListening(any()) } answers { }
        every { PitchDetector.stopListening() } answers { }
        // Simulamos que siempre detecta la nota correcta
        every { PitchDetector.getLastDetectedNote() } returns "C5"
    }

    @After
    fun tearDown() {
        unmockkObject(PitchDetector)
    }

    @Test
    fun viewSheetsContentAndCheckGreenSvg() {
        // Login con usuario lucia
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

        // Abrir carpeta "Basic Sheets"
        val recyclerView = onView(
            allOf(
                withId(R.id.recyclerView),
                childAtPosition(withId(R.id.constraint), 0)
            )
        )
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Seleccionar primera partitura
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

        // Comprobamos que se muestra la partitura
        onView(
            allOf(
                withId(R.id.sheetImageView),
                withParent(withParent(withId(R.id.fragmentContainerView))),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        // Obtenemos el ViewModel y comprobamos el SVG
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
            assertTrue("SVG should contain green when note is correct", svgContent.contains("#00FF00"))
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

            override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
