
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tfg.uniovi.melodies.model.Colors
import tfg.uniovi.melodies.model.HistoryEntry
import tfg.uniovi.melodies.fragments.viewmodels.FolderDTO
import tfg.uniovi.melodies.fragments.viewmodels.MusicXMLDTO
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.repositories.DBException
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import tfg.uniovi.melodies.repositories.UsersFirestore
import kotlin.test.assertFailsWith


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(RobolectricTestRunner::class)
class FirestoreRepositoryUnitTests {

    private lateinit var mockUsersCollection: CollectionReference
    private lateinit var mockUserDoc: DocumentReference
    private lateinit var mockFolders: CollectionReference
    private lateinit var mockFolderDoc: DocumentReference
    private lateinit var mockSheets: CollectionReference
    private lateinit var repo: FoldersAndSheetsFirestore
    private lateinit var userRepo: UsersFirestore
    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockContext: Context

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

        mockDb = mockk()
        every { mockDb.collection("users") } returns mockUsersCollection

        userRepo = UsersFirestore(mockDb)
        repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        mockContext = mockk()
        mockkStatic(PreferenceManager::class)
    }
    @BeforeEach
    fun setupLog() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

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
    /*Get Folder by Id*/

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
    fun getFolderByIdThrowsDBExceptionWhenFirestoreFails() = runTest {
        every { mockFolders.document("folderId").get() } throws Exception("Firestore read error")

        val exception = assertFailsWith<DBException> {
            repo.getFolderById("folderId")
        }
        assert(exception.message!!.contains("Folder with id folderId could not be found"))
        assert(exception.message!!.contains("Firestore read error"))
    }

    /*Get Sheet By Id*/

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
    fun getSheetByIdThrowsDBExceptionWhenFirestoreFails() = runTest {
        every { mockSheets.document("sheetId").get() } throws Exception("Firestore read error")

        val exception = assertFailsWith<DBException> {
            repo.getSheetById("sheetId", "folderId")
        }
        assert(exception.message!!.contains("Sheet with id sheetId could not be found"))
        assert(exception.message!!.contains("Firestore read error"))
    }

    /*Get All Folders*/
    @Test
    fun getAllFoldersReturnsListOfFolders() = runTest {
        // Mock folder 1
        val doc1 = mockk<DocumentSnapshot>()
        every { doc1.id } returns "folder1"
        every { doc1.data } returns mapOf("name" to "Folder One", "color" to "BLUE")

        //Mock folder 2
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
    @Test
    fun getAllFoldersReturnsEmptyList() = runTest {

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf()

        val mockQuery = mockk<Query>()
        val task: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)
        every { mockQuery.get() } returns task
        every { mockFolders.orderBy("creationTime", Query.Direction.ASCENDING) } returns mockQuery

        val folders = repo.getAllFolders()

        assertEquals(0, folders.size)
    }

    @Test
    fun getAllFoldersThrowsDBExceptionWhenFirestoreFails() = runTest {
        val mockQuery = mockk<Query>()
        every { mockQuery.get() } throws Exception("Firestore read error")
        every { mockFolders.orderBy("creationTime", Query.Direction.ASCENDING) } returns mockQuery

        val exception = assertFailsWith<DBException> {
            repo.getAllFolders()
        }

        // Verificaciones flexibles del mensaje
        assert(exception.message!!.contains("Folders could not be retrieved"))
        assert(exception.message!!.contains("Firestore read error"))
    }
    /*Add Folder*/
    @Test
    fun addFolderReturnsGeneratedIdWhenSuccess() = runTest {
        val dto = FolderDTO(
            name = "New Folder",
            color = Colors.BLUE
        )

        val mockAddedDocRef = mockk<DocumentReference>()
        every { mockAddedDocRef.id } returns "generatedId"

        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders

        val task: Task<DocumentReference> = Tasks.forResult(mockAddedDocRef)
        every { mockFolders.add(any()) } returns task

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val result = repo.addFolder(dto)

        assertEquals("generatedId", result)
    }

    @Test
    fun addFolderThrowsDBExceptionWhenFails() = runTest {
        val dto = FolderDTO(
            name = "Error Folder",
            color = Colors.PINK
        )

        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders

        // Simular fallo en add()
        every { mockFolders.add(any()) } throws Exception("Firestore error")

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.addFolder(dto)
        }

        // The exception is thrown with corresponding message
        assert(exception.message!!.contains("Error Folder could not be added"))
        assert(exception.message!!.contains("Firestore error"))
    }
    /*Add MusicXMLSheet */
    @Test
    fun addSheetReturnsGeneratedIdWhenSuccess() = runTest {
        val dto = MusicXMLDTO(
            name = "New Sheet",
            author = "Anonymous",
            stringSheet ="<score-partwise>...</score-partwise>",
        )
        dto.folderId = "folderId"
        // Mock de referencias
        val mockAddedDocRef = mockk<DocumentReference>()
        every { mockAddedDocRef.id } returns "generatedId"

        val mockSheets = mockk<CollectionReference>()
        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()


        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document("folderId") } returns mockFolderDoc
        every { mockFolderDoc.collection("sheets") } returns mockSheets

        val task: Task<DocumentReference> = Tasks.forResult(mockAddedDocRef)
        every { mockSheets.add(any()) } returns task

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val result = repo.addMusicXMLSheet(dto)

        assertEquals("generatedId", result)
    }
    @Test
    fun addSheetThrowsDBExceptionWhenFirestoreFails() = runTest {
        val dto = MusicXMLDTO(
            name = "New Sheet",
            author = "Anonymous",
            stringSheet ="<score-partwise>...</score-partwise>",
        )
        dto.folderId = "folderId"
        // Mock de referencias
        val mockAddedDocRef = mockk<DocumentReference>()
        every { mockAddedDocRef.id } returns "generatedId"

        val mockSheets = mockk<CollectionReference>()
        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()


        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document("folderId") } returns mockFolderDoc
        every { mockFolderDoc.collection("sheets") } returns mockSheets

        val task: Task<DocumentReference> = Tasks.forResult(mockAddedDocRef)
        every { mockSheets.add(any()) } throws Exception("Firestore write error")

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.addMusicXMLSheet(dto)
        }

        assert(exception.message!!.contains("New Sheet could not be added"))
        assert(exception.message!!.contains("Firestore write error"))
    }

    /* setNewFolderName*/
    @Test
    fun setNewFolderNameUpdatesSuccessfully() = runTest {
        val folderId = "folderId"
        val newName = "Updated Folder"

        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc

        val task: Task<Void> = Tasks.forResult(null)
        every { mockFolderDoc.update("name", newName) } returns task

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        repo.setNewFolderName(folderId, newName)

        verify { mockFolderDoc.update("name", newName) }
    }

    @Test
    fun setNewFolderNameThrowsDBExceptionWhenFirestoreFails() = runTest {
        val folderId = "folderId"
        val newName = "Updated Folder"

        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc

        every { mockFolderDoc.update("name", newName) } throws Exception("Firestore update error")

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.setNewFolderName(folderId, newName)
        }

        assert(exception.message!!.contains("$folderId could not be renamed to $newName"))
        assert(exception.message!!.contains("Firestore update error"))
    }


    /* setNewSheetName */
    @Test
    fun setNewSheetNameUpdatesSuccessfully() = runTest {
        val sheetId = "sheetId"
        val folderId = "folderId"
        val newName = "Updated Sheet"

        // Mocks de Firestore
        val mockDocRef = mockk<DocumentReference>()
        val mockSheets = mockk<CollectionReference>()
        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc
        every { mockFolderDoc.collection("sheets") } returns mockSheets
        every { mockSheets.document(sheetId) } returns mockDocRef

        val task: Task<Void> = Tasks.forResult(null)
        every { mockDocRef.update("name", newName) } returns task

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        repo.setNewSheetName(sheetId, folderId, newName)

        verify { mockDocRef.update("name", newName) }
    }

    @Test
    fun setNewSheetNameThrowsDBExceptionWhenFirestoreFails() = runTest {
        val sheetId = "sheetId"
        val folderId = "folderId"
        val newName = "Updated Sheet"

        val mockDocRef = mockk<DocumentReference>()
        val mockSheets = mockk<CollectionReference>()
        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc
        every { mockFolderDoc.collection("sheets") } returns mockSheets
        every { mockSheets.document(sheetId) } returns mockDocRef

        every { mockDocRef.update("name", newName) } throws Exception("Firestore update error")

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.setNewSheetName(sheetId, folderId, newName)
        }

        assert(exception.message!!.contains("$sheetId could not be renamed to $newName"))
        assert(exception.message!!.contains("Firestore update error"))
    }

    /* deleteFolder */
    @Test
    fun deleteFolderDeletesSuccessfully() = runTest {
        val folderId = "folderId"

        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc

        val task: Task<Void> = Tasks.forResult(null)
        every { mockFolderDoc.delete() } returns task

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        repo.deleteFolder(folderId)

        verify { mockFolderDoc.delete() }
    }
    @Test
    fun deleteFolderThrowsDBExceptionWhenFails() = runTest {
        val folderId = "folderId"

        // Mocks de Firestore
        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc

        every { mockFolderDoc.delete() } throws Exception("Firestore delete error")

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.deleteFolder(folderId)
        }

        assert(exception.message!!.contains("$folderId could not be deleted"))
        assert(exception.message!!.contains("Firestore delete error"))
    }

    /* Delete Sheet*/
    @Test
    fun deleteSheetDeletesSuccessfully() = runTest {
        val folderId = "folderId"
        val sheetId = "sheetId"

        // Mocks de Firestore
        val mockDocRef = mockk<DocumentReference>()
        val mockSheets = mockk<CollectionReference>()
        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc
        every { mockFolderDoc.collection("sheets") } returns mockSheets
        every { mockSheets.document(sheetId) } returns mockDocRef

        // Mock delete() exitoso
        val task: Task<Void> = Tasks.forResult(null)
        every { mockDocRef.delete() } returns task

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        repo.deleteSheet(sheetId, folderId)

        verify { mockDocRef.delete() }
    }
    @Test
    fun deleteSheetThrowsDBExceptionWhenFails() = runTest {
        val folderId = "folderId"
        val sheetId = "sheetId"

        val mockDocRef = mockk<DocumentReference>()
        val mockSheets = mockk<CollectionReference>()
        val mockFolderDoc = mockk<DocumentReference>()
        val mockFolders = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("folders") } returns mockFolders
        every { mockFolders.document(folderId) } returns mockFolderDoc
        every { mockFolderDoc.collection("sheets") } returns mockSheets
        every { mockSheets.document(sheetId) } returns mockDocRef

        every { mockDocRef.delete() } throws Exception("Firestore delete error")

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.deleteSheet(sheetId, folderId)
        }

        assert(exception.message!!.contains("$sheetId could not be deleted"))
        assert(exception.message!!.contains("Firestore delete error"))
    }
    /* getAllSheetsFromFolder */
    @Test
    fun getAllSheetsFromFolderReturnsListOfSheets() = runTest {
        val folderId = "folderId"

        // Mock documentos de sheets
        val doc1 = mockk<DocumentSnapshot>()
        every { doc1.id } returns "sheet1"
        every { doc1.data } returns mapOf(
            "name" to "Sheet One",
            "musicxml" to "<score-partwise>...</score-partwise>",
            "author" to "Author1"
        )

        val doc2 = mockk<DocumentSnapshot>()
        every { doc2.id } returns "sheet2"
        every { doc2.data } returns mapOf(
            "name" to "Sheet Two",
            "musicxml" to "<score-partwise>...</score-partwise>",
            "author" to "Author2"
        )

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(doc1, doc2)

        val mockQuery = mockk<Query>()
        val task: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)
        every { mockQuery.get() } returns task

        val mockSheets = mockk<CollectionReference>()
        every { mockSheets.get() } returns task

        val mockFolderDoc = mockk<DocumentReference>()
        every { mockFolderDoc.collection("sheets") } returns mockSheets

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.document(folderId) } returns mockFolderDoc

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val sheets = repo.getAllSheetsFromFolder(folderId)

        assertEquals(2, sheets.size)
        assertEquals("Sheet One", sheets[0].name)
        assertEquals("Sheet Two", sheets[1].name)
    }
    @Test
    fun getAllSheetsFromFolderReturnsEmptyList() = runTest {
        val folderId = "folderId"

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns emptyList()

        val mockSheets = mockk<CollectionReference>()
        val task: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)
        every { mockSheets.get() } returns task

        val mockFolderDoc = mockk<DocumentReference>()
        every { mockFolderDoc.collection("sheets") } returns mockSheets

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.document(folderId) } returns mockFolderDoc

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val sheets = repo.getAllSheetsFromFolder(folderId)

        assertEquals(0, sheets.size)
    }
    @Test
    fun getAllSheetsFromFolderThrowsDBExceptionWhenFails() = runTest {
        val folderId = "folderId"

        val mockSheets = mockk<CollectionReference>()
        every { mockSheets.get() } throws Exception("Firestore read error")

        val mockFolderDoc = mockk<DocumentReference>()
        every { mockFolderDoc.collection("sheets") } returns mockSheets

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.document(folderId) } returns mockFolderDoc

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.getAllSheetsFromFolder(folderId)
        }

        assert(exception.message!!.contains("$folderId sheets could not be retrieved"))
        assert(exception.message!!.contains("Firestore read error"))
    }
    /*isFolderNameInUse*/
    @Test
    fun isFolderNameInUseReturnsTrueWithExistingFolder() = runTest {
        val folderName = "ExistingFolder"

        // Mock a folder document with the given name
        val folderDoc = mockk<DocumentSnapshot>()
        every { folderDoc.id } returns "folder1"
        every { folderDoc.getString("name") } returns folderName

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(folderDoc)
        every { querySnapshot.isEmpty } returns false

        val query = mockk<Query>()
        every { query.get() } returns Tasks.forResult(querySnapshot)

        val foldersColl = mockk<CollectionReference>()
        every { foldersColl.whereEqualTo("name", folderName) } returns query

        val userDoc = mockk<DocumentReference>()
        every { userDoc.collection("folders") } returns foldersColl

        val usersColl = mockk<CollectionReference>()
        every { usersColl.document("userId") } returns userDoc

        val repo = FoldersAndSheetsFirestore("userId", usersColl)

        val result = repo.isFolderNameInUse(folderName)
        assert(result!!)
    }

    @Test
    fun isFolderNameInUseReturnsFalseWhenFolderDoesNotExist() = runTest {
        val folderName = "NewFolder"

        // No folder with this name
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf() // empty list
        every { querySnapshot.isEmpty } returns true

        val query = mockk<Query>()
        every { query.get() } returns Tasks.forResult(querySnapshot)

        val foldersColl = mockk<CollectionReference>()
        every { foldersColl.whereEqualTo("name", folderName) } returns query

        val userDoc = mockk<DocumentReference>()
        every { userDoc.collection("folders") } returns foldersColl

        val usersColl = mockk<CollectionReference>()
        every { usersColl.document("userId") } returns userDoc


        val repo = FoldersAndSheetsFirestore("userId", usersColl)

        val result = repo.isFolderNameInUse(folderName)
        assertFalse(result!!) // should return false because no folder exists
    }

    @Test
    fun isFolderNameInUseThrowsDBExceptionWhenFails() = runTest {
        val folderName = "AnyFolder"

        val mockQuery = mockk<Query>()
        every { mockQuery.get() } throws Exception("Firestore query failed")

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.whereEqualTo("name", folderName) } returns mockQuery

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.isFolderNameInUse(folderName)
        }

        assert(exception.message!!.contains("Unexpected exception while looking for duplicates"))
    }
    /* isSheetNameInUse */
    @Test
    fun isSheetNameInUseReturnsTrueWhenExists() = runTest {
        val folderId = "folderId"
        val sheetName = "ExistingSheet"

        // Mock a sheet document with the given name
        val sheetDoc = mockk<DocumentSnapshot>()
        every { sheetDoc.id } returns "sheet1"
        every { sheetDoc.getString("name") } returns sheetName
        every { sheetDoc.data } returns mapOf("name" to sheetName)

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(sheetDoc)
        every { querySnapshot.isEmpty } returns false

        val query = mockk<Query>()
        every { query.get() } returns Tasks.forResult(querySnapshot)

        val sheetsColl = mockk<CollectionReference>()
        every { sheetsColl.whereEqualTo("name", sheetName) } returns query

        val folderDoc = mockk<DocumentReference>()
        every { folderDoc.collection("sheets") } returns sheetsColl

        val foldersColl = mockk<CollectionReference>()
        every { foldersColl.document(folderId) } returns folderDoc

        val userDoc = mockk<DocumentReference>()
        every { userDoc.collection("folders") } returns foldersColl

        val usersColl = mockk<CollectionReference>()
        every { usersColl.document("userId") } returns userDoc


        val repo = FoldersAndSheetsFirestore("userId", usersColl)

        val result = repo.isSheetNameInUse(sheetName, folderId)
        assert(result!!)
    }
    @Test
    fun isSheetNameInUseReturnsFalseWhenNotExists() = runTest {
        val folderId = "folderId"
        val sheetName = "NewSheet"

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf() // empty
        every { querySnapshot.isEmpty } returns true

        val query = mockk<Query>()
        every { query.get() } returns Tasks.forResult(querySnapshot)

        val sheetsColl = mockk<CollectionReference>()
        every { sheetsColl.whereEqualTo("name", sheetName) } returns query

        val folderDoc = mockk<DocumentReference>()
        every { folderDoc.collection("sheets") } returns sheetsColl

        val foldersColl = mockk<CollectionReference>()
        every { foldersColl.document(folderId) } returns folderDoc

        val userDoc = mockk<DocumentReference>()
        every { userDoc.collection("folders") } returns foldersColl

        val usersColl = mockk<CollectionReference>()
        every { usersColl.document("userId") } returns userDoc

        val repo = FoldersAndSheetsFirestore("userId", usersColl)

        val result = repo.isSheetNameInUse(sheetName, folderId)
        assertFalse(result!!)
    }

    @Test
    fun isSheetNameInUseThrowsDBExceptionWhenFails() = runTest {
        val folderId = "folderId"
        val sheetName = "AnySheet"

        val query = mockk<Query>()
        every { query.get() } throws Exception("Firestore query failed")

        val sheetsColl = mockk<CollectionReference>()
        every { sheetsColl.whereEqualTo("name", sheetName) } returns query

        val folderDoc = mockk<DocumentReference>()
        every { folderDoc.collection("sheets") } returns sheetsColl

        val foldersColl = mockk<CollectionReference>()
        every { foldersColl.document(folderId) } returns folderDoc

        val userDoc = mockk<DocumentReference>()
        every { userDoc.collection("folders") } returns foldersColl

        val usersColl = mockk<CollectionReference>()
        every { usersColl.document("userId") } returns userDoc

        val repo = FoldersAndSheetsFirestore("userId", usersColl)

        val exception = assertFailsWith<DBException> {
            repo.isSheetNameInUse(sheetName, folderId)
        }

        assert(exception.message!!.contains("Unexpected exception while looking for duplicates"))
    }
    /* getFolderColor*/
    @Test
    fun getFolderColorReturnsColorWhenFolderExists() = runTest {
        val folderId = "folderId"

        val mockDoc = mockk<DocumentSnapshot>()
        every { mockDoc.exists() } returns true
        every { mockDoc.getString("color") } returns "blue"

        val mockDocRef = mockk<DocumentReference>()
        every { mockDocRef.get() } returns Tasks.forResult(mockDoc)

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.document(folderId) } returns mockDocRef

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val color = repo.getFolderColor(folderId)
        assertEquals(Colors.BLUE, color)
    }
    @Test
    fun getFolderColorReturnsNullWhenFolderDoesNotExist() = runTest {
        val folderId = "folderId"

        val mockDoc = mockk<DocumentSnapshot>()
        every { mockDoc.exists() } returns false

        val mockDocRef = mockk<DocumentReference>()
        every { mockDocRef.get() } returns Tasks.forResult(mockDoc)

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.document(folderId) } returns mockDocRef

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val color = repo.getFolderColor(folderId)
        assertNull(color)
    }
    @Test
    fun getFolderColorThrowsDBExceptionWhenFirestoreFails() = runTest {
        val folderId = "folderId"

        val mockDocRef = mockk<DocumentReference>()
        every { mockDocRef.get() } throws Exception("Firestore read error")

        val mockFolders = mockk<CollectionReference>()
        every { mockFolders.document(folderId) } returns mockDocRef

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.collection("folders") } returns mockFolders

        val mockUsersCollection = mockk<CollectionReference>()
        every { mockUsersCollection.document("userId") } returns mockUserDoc

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.getFolderColor(folderId)
        }

        assert(exception.message!!.contains("Could not retrieve color"))
        assert(exception.message!!.contains("Firestore read error"))
    }
/* saveNewHistoryEntrySucceds*/
    @Test
    fun saveNewHistoryEntrySucceeds() = runTest {
        val historyEntry = HistoryEntry(
            nameOfSheet = "Test Sheet",
            formattedTime = "12min 34s"
        )

        val mockHistoryColl = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("historyEntries") } returns mockHistoryColl

        val mockAddedDocRef = mockk<DocumentReference>()
        val task: Task<DocumentReference> = Tasks.forResult(mockAddedDocRef)
        every { mockHistoryColl.add(any()) } returns task

        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        repo.saveNewHistoryEntry(historyEntry)
    }

    @Test
    fun saveNewHistoryEntryThrowsDBExceptionWhenFails() = runTest {
        val historyEntry = HistoryEntry(
            nameOfSheet = "Test Sheet",
            formattedTime = "12min 34s"
        )

        val mockHistoryColl = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockUserDoc.collection("historyEntries") } returns mockHistoryColl

        // Force Firestore to fail
        every { mockHistoryColl.add(any()) } throws Exception("Firestore write error")


        val repo = FoldersAndSheetsFirestore("userId", mockUsersCollection)

        val exception = assertFailsWith<DBException> {
            repo.saveNewHistoryEntry(historyEntry)
        }

        assert(exception.message!!.contains("History entry could not be saved"))
        assert(exception.message!!.contains("Firestore write error"))
    }
    /*getAllHistoryEntries*/
    @Test
    fun getAllHistoryEntriesReturnsList() = runTest {
        val mockDb = mockk<FirebaseFirestore>()
        val mockUsersCollection = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockHistoryColl = mockk<CollectionReference>()
        val mockQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()

        // Mock de documentos
        every { doc1.getString("nameOfSheet") } returns "Sheet1"
        every { doc1.getString("formattedTime") } returns "12:00"
        every { doc2.getString("nameOfSheet") } returns "Sheet2"
        every { doc2.getString("formattedTime") } returns "13:00"

        // Mock del snapshot
        every { querySnapshot.documents } returns listOf(doc1, doc2)
        every { mockQuery.get() } returns Tasks.forResult(querySnapshot)
        every { mockQuery.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
        every { mockQuery.limit(5) } returns mockQuery

        // Mock de la colección historyEntries
        every { mockHistoryColl.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
        every { mockUserDoc.collection("historyEntries") } returns mockHistoryColl

        // Mock del documento de usuario y colección users
        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockDb.collection("users") } returns mockUsersCollection

        val userRepo = UsersFirestore(mockDb)
        val entries = userRepo.getAllHistoryEntries("userId")

        assertEquals(2, entries.size)
        assertEquals("Sheet1", entries[0].nameOfSheet)
        assertEquals("12:00", entries[0].formattedTime)
        assertEquals("Sheet2", entries[1].nameOfSheet)
        assertEquals("13:00", entries[1].formattedTime)
    }


    @Test
    fun getAllHistoryEntriesReturnsEmptyListWhenNoDocuments() = runTest {
        val mockUsersCollection = mockk<CollectionReference>()
        val mockUserDoc = mockk<DocumentReference>()
        val mockHistoryColl = mockk<CollectionReference>()
        val mockQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { querySnapshot.documents } returns listOf()

        every { mockQuery.get() } returns Tasks.forResult(querySnapshot)
        every { mockQuery.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
        every { mockQuery.limit(5) } returns mockQuery

        every { mockHistoryColl.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
        every { mockUserDoc.collection("historyEntries") } returns mockHistoryColl

        every { mockUsersCollection.document("userId") } returns mockUserDoc
        every { mockDb.collection("users") } returns mockUsersCollection

        val repo = UsersFirestore(mockDb)
        val entries = repo.getAllHistoryEntries("userId")

        assert(entries.isEmpty())
    }


    @Test
    fun getAllHistoryEntriesThrowsDBExceptionWhenFirestoreFails() = runTest {
        val mockUsersCollection = mockk<CollectionReference>()

        // Configuramos que la llamada a document() lance una excepción
        every { mockDb.collection("users") } returns mockUsersCollection
        every { mockUsersCollection.document("userId") } throws RuntimeException("Firestore failure")

        val userRepo = UsersFirestore(mockDb)

        // Ejecutamos y verificamos que lance DBException
        val exception = assertFailsWith<DBException> {
            userRepo.getAllHistoryEntries("userId")
        }

        // Verificamos que el mensaje contenga la información esperada
        assertTrue(exception.message!!.contains("History entries could not be retrieved"))
        assertTrue(exception.message!!.contains("Firestore failure"))
    }


    /* nicknameExists */
    @Test
    fun nicknameExistsReturnsTrueWhenNicknameTaken() = runTest {
        val nickname = "existingNick"

        val mockDb = mockk<FirebaseFirestore>()
        val mockUsersCollection = mockk<CollectionReference>()
        val mockQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        // Mockeamos correctamente el método isEmpty()
        every { querySnapshot.isEmpty } returns false
        every { mockQuery.get() } returns Tasks.forResult(querySnapshot)
        every { mockQuery.limit(1) } returns mockQuery
        every { mockUsersCollection.whereEqualTo("nickname", nickname) } returns mockQuery
        every { mockDb.collection("users") } returns mockUsersCollection

        val repo = UsersFirestore(mockDb)
        val result = repo.nicknameExists(nickname)

        assert(result)
    }



    @Test
    fun nicknameExistsReturnsFalseWhenNicknameAvailable() = runTest {
        val nickname = "newNick"

        val mockDb = mockk<FirebaseFirestore>()
        val mockUsersCollection = mockk<CollectionReference>()
        val mockQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { querySnapshot.isEmpty } returns true
        every { mockQuery.get() } returns Tasks.forResult(querySnapshot)
        every { mockQuery.limit(1) } returns mockQuery
        every { mockUsersCollection.whereEqualTo("nickname", nickname) } returns mockQuery
        every { mockDb.collection("users") } returns mockUsersCollection

        val result = userRepo.nicknameExists(nickname)

        assertFalse(result)
    }


    @Test
    fun nicknameExistsReturnsFalseOnFirestoreException() = runTest {
        val nickname = "anyNick"

        val mockDb = mockk<FirebaseFirestore>()
        val mockUsersCollection = mockk<CollectionReference>()

        every { mockDb.collection("users") } returns mockUsersCollection
        every { mockUsersCollection.whereEqualTo("nickname", nickname) } throws RuntimeException("Firestore failure")

        val result = userRepo.nicknameExists(nickname)

        assertFalse(result)
    }
    /*updateUserNickname*/
    @Test
    fun updateUserNicknameSucceeds() = runTest {
        every { mockUserDoc.update("nickname", "newNick") } returns Tasks.forResult(null)

        userRepo.updateUserNickname("userId", "newNick")

        verify { mockUserDoc.update("nickname", "newNick") }
    }

    @Test
    fun updateUserNicknameThrowsDBExceptionOnFirestoreFailure() = runTest {
        every { mockUserDoc.update("nickname", "newNick") } throws Exception("Firestore update error")

        val exception = assertFailsWith<DBException> {
            userRepo.updateUserNickname("userId", "newNick")
        }

        assert(exception.message!!.contains("Error while updating userId updating"))
        assert(exception.message!!.contains("Firestore update error"))
    }
    /* getUserIdFromNickname*/

    @Test
    fun getUserIdFromNicknameReturnsUserIdWhenFound() = runTest {
        val mockDoc = mockk<DocumentSnapshot>()
        every { mockDoc.id } returns "user123"

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.isEmpty } returns false
        every { querySnapshot.documents } returns listOf(mockDoc)

        every { mockUsersCollection.whereEqualTo("nickname", "nick") } returns mockUsersCollection
        every { mockUsersCollection.limit(1) } returns mockUsersCollection
        every { mockUsersCollection.get() } returns Tasks.forResult(querySnapshot)

        val result = userRepo.getUserIdFromNickname("nick")
        assertEquals("user123", result)
    }

    @Test
    fun getUserIdFromNicknameReturnsNullWhenNotFound() = runTest {
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.isEmpty } returns true

        every { mockUsersCollection.whereEqualTo("nickname", "nick") } returns mockUsersCollection
        every { mockUsersCollection.limit(1) } returns mockUsersCollection
        every { mockUsersCollection.get() } returns Tasks.forResult(querySnapshot)

        val result = userRepo.getUserIdFromNickname("nick")
        assertEquals(null, result)
    }

    @Test
    fun getUserIdFromNicknameThrowsDBExceptionOnFirestoreFailure() = runTest {
        val exception = RuntimeException("Firestore write error")

        every { mockUsersCollection.whereEqualTo("nickname", "nick") } returns mockUsersCollection
        every { mockUsersCollection.limit(1) } returns mockUsersCollection
        every { mockUsersCollection.get() } returns Tasks.forException(exception)

        val result = userRepo.getUserIdFromNickname("nick")
        assertEquals(null, result)
    }
    @Test
    fun getNicknameFromUserIdReturnsNicknameWhenFound() = runTest {
        val docSnapshot = mockk<DocumentSnapshot>()
        every { docSnapshot.exists() } returns true
        every { docSnapshot.getString("nickname") } returns "nickname123"

        every { mockUserDoc.get() } returns Tasks.forResult(docSnapshot)

        val result = userRepo.getNicknameFromUserId("userId")
        assertEquals("nickname123", result)
    }

    @Test
    fun getNicknameFromUserIdReturnsNullWhenNotFound() = runTest {
        val docSnapshot = mockk<DocumentSnapshot>()
        every { docSnapshot.exists() } returns false

        every { mockUserDoc.get() } returns Tasks.forResult(docSnapshot)

        val result = userRepo.getNicknameFromUserId("userId")
        assertEquals(null, result)
    }

    @Test
    fun getNicknameFromUserIdReturnsNullOnFirestoreFailure() = runTest {
        val exception = RuntimeException("Firestore read error")
        every { mockUserDoc.get() } returns Tasks.forException(exception)

        val result = userRepo.getNicknameFromUserId("userId")
        assertEquals(null, result)
    }

    @Test
    fun setupUserDataIfNeededReturnsNullIfUserExists() = runTest {
        every { PreferenceManager.getUserId(mockContext) } returns "existingUserId"

        val result = userRepo.setupUserDataIfNeeded("nickname", mockContext)
        assertEquals(null, result)
        verify(exactly = 0) { mockDb.collection(any()) }
    }
    @Test
    fun `setupUserDataIfNeeded creates user successfully`() = runTest {
        val mockPrefs = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>()

        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockPrefs.getString(any(), any()) } returns null

        val mockDb = mockk<FirebaseFirestore>()
        val mockUserRef = mockk<DocumentReference>()
        val mockFolderRef = mockk<DocumentReference>()
        val mockFoldersColl = mockk<CollectionReference>()
        val mockSheetsColl = mockk<CollectionReference>()
        val mockDefaultSheetsColl = mockk<CollectionReference>()
        val mockQuerySnapshot = mockk<QuerySnapshot>()
        val mockSheetDoc1 = mockk<QueryDocumentSnapshot>()
        val mockSheetDoc2 = mockk<QueryDocumentSnapshot>()

        val userSlot = slot<Map<String, Any>>()
        val folderSlot = slot<Map<String, Any>>()
        val addedSheets = mutableListOf<Map<String, Any>>()

        every { mockDb.collection("users") } returns mockk {
            every { document() } returns mockUserRef
        }
        every { mockUserRef.id } returns "user123"
        every { mockUserRef.set(capture(userSlot)) } returns Tasks.forResult(null)
        every { mockUserRef.collection("folders") } returns mockFoldersColl
        every { mockFoldersColl.document() } returns mockFolderRef
        every { mockFolderRef.set(capture(folderSlot)) } returns Tasks.forResult(null)
        every { mockFolderRef.collection("sheets") } returns mockSheetsColl
        every { mockSheetsColl.add(any()) } answers {
            val data = firstArg<Map<String, Any>>()
            addedSheets.add(data)
            Tasks.forResult(mockk()) // Task result not null
        }

        every { mockDb.collection("defaultSheets") } returns mockDefaultSheetsColl
        every { mockDefaultSheetsColl.get() } returns Tasks.forResult(mockQuerySnapshot)
        every { mockQuerySnapshot.iterator() } returns mutableListOf(mockSheetDoc1, mockSheetDoc2).iterator()
        every { mockSheetDoc1.data } returns mapOf(
            "name" to "Sheet One",
            "musicxml" to "<score-partwise>...</score-partwise>",
            "author" to "Author1"
        )
        every { mockSheetDoc2.data } returns mapOf(
            "name" to "Sheet Two",
            "musicxml" to "<score-partwise>...</score-partwise>",
            "author" to "Author2"
        )

        val repo = UsersFirestore(mockDb)
        val userId = repo.setupUserDataIfNeeded("nickname123", mockContext)

        assertEquals("user123", userId)

        // User data
        assertEquals("nickname123", userSlot.captured["nickname"])
        assertTrue(userSlot.captured.containsKey("createdAt"))

        // Folder data
        assertEquals("Basic Sheets", folderSlot.captured["name"])
        assertEquals("YELLOW", folderSlot.captured["color"])
        assertTrue(folderSlot.captured.containsKey("creationTime"))

        // Sheets added
        assertEquals(2, addedSheets.size)
        assertTrue(addedSheets.any { it["name"] == "Sheet One" && it["author"] == "Author1" })
        assertTrue(addedSheets.any { it["name"] == "Sheet Two" && it["author"] == "Author2" })

        // Verify SharedPreferences write
        verify { mockEditor.putString("user_id", "user123") }
        verify { mockEditor.apply() }
    }



    @Test
    fun setupUserDataIfNeededThrowsDBExceptionOnFailure() = runTest {
        every { PreferenceManager.getUserId(mockContext) } returns null

        val mockUserDoc = mockk<DocumentReference>()
        every { mockUserDoc.id } returns "newUserId"
        every { mockDb.collection("users").document() } returns mockUserDoc
        every { mockUserDoc.set(any<Map<String, Any>>()) } returns Tasks.forException(RuntimeException("Firestore error"))

        assertFailsWith<DBException> {
            userRepo.setupUserDataIfNeeded("nickname", mockContext)
        }
    }

}
