package tfg.uniovi.melodies.sheetManagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.uiautomator.By
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
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
class ImportWithoutFoldersDirectTest {

    private lateinit var userRepository: UsersFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            // Ensure the test user exists
            userRepository.setupUserDataIfNeeded("import", context)
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            try {
                userRepository.deleteUserAndAllData("import")
            } catch (e: Exception) {
                println("Error deleting test user: ${e.message}")
            }
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun importWithoutFoldersShowsAlertDialog() {
        // Step 1: Log in with the user "import"
        onView(withId(R.id.input_nickname)).perform(replaceText("import"), closeSoftKeyboard())
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Step 2: Navigate to the Import screen
        onView(withId(R.id.importing)).perform(click())

        // Step 3: Click the Import button without selecting a file
        onView(withId(R.id.btnImport)).perform(click())

        // Step 4: Verify the AlertDialog with error appears
        val expectedTitle = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.no_import)
        val expectedMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.no_folders_no_import)

        device.wait(Until.hasObject(By.textContains(expectedTitle)), 2000)
        device.wait(Until.hasObject(By.textContains(expectedMessage)), 2000)

        // Optional: click OK on the AlertDialog
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()
    }
}
