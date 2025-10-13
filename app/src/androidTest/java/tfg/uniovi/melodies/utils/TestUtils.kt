package tfg.uniovi.melodies.utils
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.hamcrest.Matchers.allOf
import tfg.uniovi.melodies.R

object TestUtils {

    fun loginAs(username: String) {
        // Escribir el nickname
        onView(
            allOf(
                withId(R.id.input_nickname),
                isDisplayed()
            )
        ).perform(replaceText(username), closeSoftKeyboard())

        // Pulsar el botón de login
        onView(
            allOf(
                withId(R.id.btnEnterPreviousAccount),
                withText("Log In"),
                isDisplayed()
            )
        ).perform(click())

        // Esperar y verificar que se muestra la pantalla principal
        onView(withId(R.id.tv_folder_title))
            .check(matches(isDisplayed()))
    }
}
