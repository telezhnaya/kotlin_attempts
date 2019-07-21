package observer

import TestBase
import org.junit.Test


class LocalFileSystemTest : TestBase() {
    @Test
    fun `getCurrentFileName() gives correct name`() {
        assert(fileSystem.getCurrentFileName() == rootDir.root.name)
    }

    @Test
    fun `getFullPath() gives correct path`() {
        assert(fileSystem.getFullPath() == rootDir.root.absolutePath)
    }

    @Test
    fun `getFileList() gives all needed files in right order`() {
        assert(fileSystem.getFileList() == listOf(SUB_DIR, FILE_JPG, FILE_TXT, FILE_UNTYPED))
    }

    @Test
    fun `goForward() changes current directory to chosen one`() {
        fileSystem = fileSystem.goForward(SUB_DIR)!!
        assert(fileSystem.getCurrentFileName() == SUB_DIR)
    }

    @Test
    fun `full path changed after goForward()`() {
        this.`goForward() changes current directory to chosen one`()
        assert(fileSystem.getFullPath() == rootDir.root.resolve(SUB_DIR).absolutePath)
    }

    @Test
    fun `goForward(notADirectory) does not change current directory`() {
        val currentPath = fileSystem.getFullPath()
        assert(fileSystem.goForward(FILE_JPG) == null)
        assert(currentPath == fileSystem.getFullPath())
    }

    @Test
    fun `goBack() changes directory to parent's one`() {
        this.`goForward() changes current directory to chosen one`()
        val root = fileSystem.goBack()!!
        assert(root.getCurrentFileName() == rootDir.root.name)
    }

    @Test
    fun `getPreview() gives correct type of output`() {
        assert(fileSystem.getPreview() is PreviewData.Directory)
        assert(fileSystem.getPreview(FILE_TXT) is PreviewData.Text)
        assert(fileSystem.getPreview(FILE_JPG) is PreviewData.Image)
        assert(fileSystem.getPreview(FILE_UNTYPED) is PreviewData.Unhandled)
        // not sure that it was great architectural decision, but anyway it's useful to have such test
        // we need to know if the behavior changed
        assert(fileSystem.getPreview(FILE_NOT_EXIST) is PreviewData.Unhandled)
    }

    @Test
    fun `goBack() reaches root and stops after it`() {
        var path = fileSystem.getFullPath()
        while (fileSystem.goBack() != null) {
            val current = fileSystem.getFullPath()
            assert(path.startsWith(current))
            assert(path != current)
            path = current
        }
        assert(path.isEmpty())
        assert(fileSystem.goBack() == null)
        assert(fileSystem.getFullPath().isEmpty())
    }

    @Test
    fun `file list is not empty after reaching root`() {
        this.`goBack() reaches root and stops after it`()
        assert(fileSystem.getCurrentFileName() == "")
        assert(fileSystem.getFileList().isNotEmpty())
    }
}