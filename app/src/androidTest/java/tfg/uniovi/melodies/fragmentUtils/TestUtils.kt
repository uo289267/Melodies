package tfg.uniovi.melodies.fragmentUtils
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.repositories.UsersFirestore

class TestUtils {
    companion object {
        @JvmStatic
    fun loginAs(username: String) {
        onView(withId(R.id.input_nickname)).perform(
            replaceText(username),
            closeSoftKeyboard()
        )
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())
    }
    /**
     * Simula el proceso de logout mediante Espresso.
     * Si el usuario no está logueado, simplemente ignora los fallos.
     */
    @JvmStatic
    fun performLogoutIfPossible() {
        try {
            // Open Profile Screen
            onView(
                allOf(
                    withId(R.id.profile),
                    withContentDescription("Profile"),
                    isDisplayed()
                )
            ).perform(click())

            // Click Logout button
            onView(
                allOf(
                    withId(R.id.btnLogOut),
                    withText("Log out"),
                    isDisplayed()
                )
            ).perform(click())

            // Confirm logout action
            onView(
                allOf(
                    withId(android.R.id.button1),
                    withText("OK")
                )
            ).perform(scrollTo(), click())

            // Verify login
            onView(withId(R.id.tvLogInTitle))
                .check(matches(withText("Log In")))

            Thread.sleep(500)
        } catch (e: Exception) {
            println("Logout no ejecutado (posiblemente ya no había sesión activa): ${e.message}")
        }
    }
        @JvmStatic
    fun setupTestUser(nick: String): UsersFirestore {
        val userRepository = UsersFirestore()
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val userId = userRepository.setupUserDataIfNeeded(nick, context)
            if(userId!=null)
                loginAs(nick)
        }
        return userRepository
    }
        @JvmStatic
    fun cleanupTestUser(nick: String) {
        val userRepository = UsersFirestore()
        runBlocking {
            try {
                performLogoutIfPossible()
                userRepository.deleteUserAndAllData(nick)
            } catch (e: Exception) {
                println("Error deleting test user: ${e.message}")
            }
        }
    }
        @JvmStatic
        fun deleteTestUser(nick: String) {
            val userRepository = UsersFirestore()
            runBlocking {
                try {
                    userRepository.deleteUserAndAllData(nick)
                } catch (e: Exception) {
                    println("Error deleting test user: ${e.message}")
                }
            }
        }
    }
}
