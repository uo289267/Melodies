package tfg.uniovi.melodies.sheetManagement.rename

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
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
class RenameSheetFromLibraryTest {
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TestUtils.setupTestUser("rename3")
    }

    @After
    fun cleanup() {
        TestUtils.cleanupTestUser("rename3")
    }


    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun renameSheetFromLibraryTest() {
        val originalSheetName = "Canción de Cuna"
        val newSheetName = "Cancion Renombrada"

        //Go to Basic Sheets folder
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Basic Sheets")), click()
                )
            )
        device.wait(Until.hasObject(By.textContains(originalSheetName)), 7000)
        //Long click the sheet to rename
        onView(withId(R.id.recycler_view_library))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(originalSheetName)), longClick()
                )
            )

        //Wait for the rename dialog
        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 2000)

        // Enter new name in the EditText
        val inputField = device.findObject(By.res("tfg.uniovi.melodies", "input"))
        inputField?.text = newSheetName

        //Click OK in the dialog
        val okButton = device.findObject(By.textContains("OK"))
        okButton?.click()
        Thread.sleep(1500)

        device.wait(Until.hasObject(By.textContains(newSheetName)), 7000)

        // Verificar que existe
            onView(withId(R.id.recycler_view_library))
                .check(matches(hasDescendant(withText(newSheetName))))
    }
}
