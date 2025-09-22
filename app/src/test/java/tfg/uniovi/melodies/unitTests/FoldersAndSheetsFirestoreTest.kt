import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore

class FoldersAndSheetsFirestoreTest {

    @Test
    fun `getFolderById returns folder when exists`() = runTest {
        val mockDoc = mockk<DocumentSnapshot>()
        every { mockDoc.exists() } returns true
        every { mockDoc.data } returns mapOf("name" to "TestFolder", "color" to "BLUE")
        every { mockDoc.id } returns "folderId"

        val mockDocRef = mockk<DocumentReference>()
        val completedTask: Task<DocumentSnapshot> = Tasks.forResult(mockDoc)
        every { mockDocRef.get() } returns completedTask

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.document("folderId") } returns mockDocRef

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val folder: Folder? = repo.getFolderById("folderId")

        assertEquals("TestFolder", folder?.name)
        assertEquals(Colors.BLUE, folder?.color)
        assertEquals("folderId", folder?.folderId)
    }


}
