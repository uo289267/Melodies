package tfg.uniovi.melodies.folderManagement
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
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
import tfg.uniovi.melodies.utils.TestUtils

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeleteFolderSuccessTest {
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("deleteFolder")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("deleteFolder")
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun deleteFolderSuccessTest() {
        val folderName = "Folder to Delete"

        // Click FAB to create a new folder
        onView(withId(R.id.fab_add_new_folder)).perform(click())
        onView(withId(R.id.folder_name_input))
            .perform(replaceText(folderName), closeSoftKeyboard())
        onView(withId(R.id.btn_create_folder)).perform(click())

        // Wait until folder appears
        device.wait(Until.hasObject(By.textContains(folderName)), 3000)

        // Long-click to open context menu
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(folderName)),
                    longClick()
                )
            )

        // Wait for delete dialog
        device.wait(Until.hasObject(By.textContains("Delete folder")), 2000)

        // Confirm deletion
        device.findObject(By.textContains("Delete folder"))?.click()

        // Wait until the folder name disappears from screen
        device.wait(Until.gone(By.textContains(folderName)), 5000)

    }


}