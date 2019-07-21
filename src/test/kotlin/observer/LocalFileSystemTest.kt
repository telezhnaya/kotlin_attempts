package observer

import observer.filesystem.LocalFileSystem
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.TemporaryFolder


internal class LocalFileSystemTest {
    private val rootDir = TemporaryFolder()
    private lateinit var fileSystem: FileSystem

    @Before
    fun setUp() {
        rootDir.create()
        rootDir.newFile(FILE_JPG)
        rootDir.newFile(FILE_TXT)
        rootDir.newFile(FILE_UNTYPED)
        rootDir.newFolder(SUB_DIR)

        fileSystem = LocalFileSystem(rootDir.root.toPath())
    }

    @Test
    fun checkCurrentFileName() {
        assert(fileSystem.getCurrentFileName() == rootDir.root.name)
    }

    @Test
    fun checkFullPath() {
        assert(fileSystem.getFullPath() == rootDir.root.absolutePath)
    }

    @Test
    fun checkFileListOrder() {
        assert(fileSystem.getFileList() == listOf(SUB_DIR, FILE_JPG, FILE_TXT, FILE_UNTYPED))
    }

    @Test
    fun checkGoForward() {
        fileSystem = fileSystem.goForward(SUB_DIR)!!
        assert(fileSystem.getCurrentFileName() == SUB_DIR)
    }

    @Test
    fun checkFullPathChanged() {
        this.checkGoForward()
        assert(fileSystem.getFullPath() == rootDir.root.resolve(SUB_DIR).absolutePath)
    }

    @Test
    fun shouldNotGoForwardToFile() {
        assert(fileSystem.goForward(FILE_JPG) == null)
    }

    @Test
    fun checkGoBack() {
        this.checkGoForward()
        val root = fileSystem.goBack()!!
        assert(root.getCurrentFileName() == rootDir.root.name)
    }

    @Test
    fun checkPreviewDataTypes() {
        assert(fileSystem.getPreview() is PreviewData.Directory)
        assert(fileSystem.getPreview(FILE_TXT) is PreviewData.Text)
        assert(fileSystem.getPreview(FILE_JPG) is PreviewData.Image)
        assert(fileSystem.getPreview(FILE_UNTYPED) is PreviewData.Unhandled)
        // not sure that it was great architectural decision, but anyway it's useful to have such test
        // we need to know if the behavior changed
        assert(fileSystem.getPreview(FILE_NOT_EXIST) is PreviewData.Unhandled)
    }

    @Test
    fun checkGoToRoot() {
        while (fileSystem.goBack() != null) {
        }
        assert(fileSystem.goBack() == null)
    }

    @Test
    fun checkRootListFiles() {
        this.checkGoToRoot()
        assert(fileSystem.getCurrentFileName() == "")
        assert(fileSystem.getFileList().isNotEmpty())
    }

    @After
    fun tearDown() {
        rootDir.delete()
    }

    companion object {
        const val SUB_DIR = "sub_dir"
        const val FILE_JPG = "root_file1.jpg"
        const val FILE_TXT = "root_file2.txt"
        const val FILE_UNTYPED = "root_file3"
        const val FILE_NOT_EXIST = "12345"
    }
}