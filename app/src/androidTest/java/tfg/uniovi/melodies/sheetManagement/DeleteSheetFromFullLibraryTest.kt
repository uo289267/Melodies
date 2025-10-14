package tfg.uniovi.melodies.sheetManagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.repositories.UsersFirestore

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeleteSheetFromFullLibraryTest {

    private lateinit var userRepository: UsersFirestore

    @Before
    fun setup() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("delete", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("delete")
            } catch (e: Exception) {
                println("⚠️ Error deleting test user: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun deleteSheetFromFullLibrary() {
        // Step 1: Log in as "delete"
        onView(withId(R.id.input_nickname)).perform(replaceText("delete"), closeSoftKeyboard())
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Step 2: Go to FullLibrary tab via Bottom Navigation
        onView(withId(R.id.fullLibrary)).perform(click())

        // Step 3: Long click on "Canción de Cuna"
        onView(withText("Canción de Cuna")).perform(longClick())

        // Step 4: Click "Delete sheet" in the dialog
        onView(withText(R.string.delete_btn)).perform(click())

        // Step 5: Verify that "Canción de Cuna" no longer exists
        Thread.sleep(2000)
        onView(withText("Canción de Cuna")).check(doesNotExist())
    }
}
