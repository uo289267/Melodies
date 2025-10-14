package tfg.uniovi.melodies.sheetManagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
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
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import tfg.uniovi.melodies.repositories.UsersFirestore

@LargeTest
@RunWith(AndroidJUnit4::class)
class ImportWithoutFoldersTest {

    private lateinit var userRepository: UsersFirestore
    private lateinit var foldersRepository: FoldersAndSheetsFirestore
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            userRepository = UsersFirestore()
            userRepository.setupUserDataIfNeeded("import", context)

            // Instantiate the folders repository with the actual userId
            val userId = userRepository.getUserIdFromNickname("import")
            foldersRepository = userId?.let { FoldersAndSheetsFirestore(it) }!!

            // Delete all existing folders for this user
            val folders = foldersRepository.getAllFolders()
            for (folder in folders) {
                foldersRepository.deleteFolder(folder.folderId)
            }
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
    fun importFileWithoutFoldersShowsAlertDialog() {
        // Step 1: Log in with the user "import"
        onView(withId(R.id.input_nickname)).perform(replaceText("import"), closeSoftKeyboard())
        onView(withId(R.id.btnEnterPreviousAccount)).perform(click())
        Thread.sleep(1000)
        onView(withId(android.R.id.button1)).perform(click())

        // Step 2: Navigate to the Import view
        onView(withId(R.id.importing)).perform(click())

        // Step 3: Click the upload file button
        onView(withId(R.id.imgBtnUploadFile)).perform(click())

        // Step 4: Select rests.xml using UiAutomator
        device.wait(Until.hasObject(By.pkg("com.android.documentsui")), 3000)
        val fileToSelect = device.findObject(By.textContains("rests.xml"))
        if (fileToSelect != null) {
            fileToSelect.click()
        } else {
            val downloads = device.findObject(By.textContains("Downloads"))
            downloads?.click()
            device.wait(Until.findObject(By.textContains("rests.xml")), 2000)
            device.findObject(By.textContains("rests.xml"))?.click()
        }

        // Step 5: Click the Import button
        device.wait(Until.hasObject(By.textContains("Import")), 2000)
        onView(withId(R.id.btnImport)).perform(click())

        // Step 6: Verify that the AlertDialog appears with the error message
        val expectedTitle = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.no_import)
        val expectedMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.no_folders_no_import)

        device.wait(Until.hasObject(By.textContains(expectedTitle)), 2000)
        device.wait(Until.hasObject(By.textContains(expectedMessage)), 2000)

        // Optional: Click OK on the AlertDialog
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()
    }
}
