package tfg.uniovi.melodies.folderManagement.rename
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tfg.uniovi.melodies.MainActivity
import tfg.uniovi.melodies.R
import tfg.uniovi.melodies.fragmentUtils.TestUtils

@LargeTest
@RunWith(AndroidJUnit4::class)
class RenameFolderSuccessTest {
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("rename")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("rename")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun renameFolderSuccessTest() {
        val originalFolderName = "Basic Sheets"
        val newFolderName = "Renamed Folder"

        // Long click on folder in Home
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(originalFolderName)), longClick()
                )
            )

        // Wait for rename dialog to appear
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 2000)

        // Enter new folder name
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "input"))
        inputField?.text = newFolderName

        // Click "OK" in dialog
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()

        device.wait(Until.hasObject(By.textContains(newFolderName)), 8000)

        // Verify folder is renamed in the list
        onView(withId(R.id.recyclerView))
            .check { view, _ ->
                val recycler = view as RecyclerView
                var found = false
                for (i in 0 until recycler.childCount) {
                    val childTextView = recycler.getChildAt(i)
                        .findViewById<android.widget.TextView>(R.id.tv_folder_title)
                    if (childTextView != null && childTextView.text.toString() == newFolderName) {
                        found = true
                        break
                    }
                }
                assert(found) { "Folder was not renamed correctly" }
            }
    }
}