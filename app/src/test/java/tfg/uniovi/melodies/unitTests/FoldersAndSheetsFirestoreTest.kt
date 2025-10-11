
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FoldersAndSheetsFirestoreTest {

    private lateinit var mockUsersCollection: CollectionReference
    private lateinit var mockUserDoc: DocumentReference
    private lateinit var mockFolders: CollectionReference
    private lateinit var mockFolderDoc: DocumentReference
    private lateinit var mockSheets: CollectionReference
    private lateinit var repo: FoldersAndSheetsFirestore

    @BeforeEach
    fun setup() {
        mockSheets = mockk()
        mockFolderDoc = mockk()
        every { mockFolderDoc.collection("sheets") } returns mockSheets

        mockFolders = mockk()
        every { mockFolders.document(any()) } returns mockFolderDoc

        mockUserDoc = mockk()
        every { mockUserDoc.collection("folders") } returns mockFolders

        mockUsersCollection = mockk()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)
    }

    // Helper para mockear folders
    private fun mockFolder(folderId: String, exists: Boolean, data: Map<String, Any>? = null) {
        val mockDoc = mockk<DocumentSnapshot>()
        every { mockDoc.exists() } returns exists
        data?.let { every { mockDoc.data } returns it }
        every { mockDoc.id } returns folderId

        val mockDocRef = mockk<DocumentReference>()
        val completedTask: Task<DocumentSnapshot> = Tasks.forResult(mockDoc)
        every { mockDocRef.get() } returns completedTask

        every { mockFolders.document(folderId) } returns mockDocRef
    }

    // Helper para mockear sheets dentro de una carpeta
    private fun mockSheet(sheetId: String, exists: Boolean, data: Map<String, Any>? = null) {
        val mockDoc = mockk<DocumentSnapshot>()
        every { mockDoc.exists() } returns exists
        data?.let { every { mockDoc.data } returns it }
        every { mockDoc.id } returns sheetId

        val mockDocRef = mockk<DocumentReference>()
        val completedTask: Task<DocumentSnapshot> = Tasks.forResult(mockDoc)
        every { mockDocRef.get() } returns completedTask

        every { mockSheets.document(sheetId) } returns mockDocRef
    }

    @Test
    fun getFolderByIdReturnsFolderWhenExists() = runTest {
        mockFolder("folderId", true, mapOf("name" to "TestFolder", "color" to "BLUE"))

        val folder = repo.getFolderById("folderId")

        assertEquals("TestFolder", folder?.name)
        assertEquals(Colors.BLUE, folder?.color)
        assertEquals("folderId", folder?.folderId)
    }

    @Test
    fun getFolderByIdReturnsNullWhenDoesNotExist() = runTest {
        mockFolder("folderId", false)

        val folder = repo.getFolderById("folderId")

        assertNull(folder)
    }

    @Test
    fun getSheetByIdReturnsSheetWhenExists() = runTest {
        val musicXML = "<score-partwise>...</score-partwise>"
        mockSheet("sheetId", true, mapOf(
            "name" to "TestSheet",
            "musicxml" to musicXML,
            "author" to "TestAuthor"
        ))

        val sheet = repo.getSheetById("sheetId", "folderId")

        assertEquals("TestSheet", sheet?.name)
        assertEquals(musicXML, sheet?.stringSheet)
        assertEquals("TestAuthor", sheet?.author)
        assertEquals("sheetId", sheet?.id)
        assertEquals("folderId", sheet?.folderId)
    }

    @Test
    fun getSheetByIdReturnsNullWhenDoesNotExist() = runTest {
        mockSheet("sheetId", false)

        val sheet = repo.getSheetById("sheetId", "folderId")

        assertNull(sheet)
    }

    @Test
    fun `getAllFolders returns list of folders`() = runTest {
        // Mock documentos
        val doc1 = mockk<DocumentSnapshot>()
        every { doc1.id } returns "folder1"
        every { doc1.data } returns mapOf("name" to "Folder One", "color" to "BLUE")

        val doc2 = mockk<DocumentSnapshot>()
        every { doc2.id } returns "folder2"
        every { doc2.data } returns mapOf("name" to "Folder Two", "color" to "PINK")

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(doc1, doc2)

        val mockQuery = mockk<Query>()
        val task: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)
        every { mockQuery.get() } returns task
        every { mockFolders.orderBy("creationTime", Query.Direction.ASCENDING) } returns mockQuery

        val folders = repo.getAllFolders()

        assertEquals(2, folders.size)
        assertEquals("Folder One", folders[0].name)
        assertEquals("Folder Two", folders[1].name)
    }

}
