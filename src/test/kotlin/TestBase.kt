import observer.FileSystem
import observer.filesystem.LocalFileSystem
import org.junit.After
import org.junit.Before
import org.junit.rules.TemporaryFolder

open class TestBase {
    val rootDir = TemporaryFolder()
    lateinit var fileSystem: FileSystem

    @Before
    open fun setUp() {
        rootDir.create()
        rootDir.newFile(FILE_JPG)
        rootDir.newFile(FILE_TXT)
        rootDir.newFile(FILE_UNTYPED)
        rootDir.newFolder(SUB_DIR)

        fileSystem = LocalFileSystem(rootDir.root.toPath())
    }

    @After
    open fun tearDown() {
        rootDir.delete()
    }

    companion object {
        const val SUB_DIR = "sub_dir"
        const val FILE_JPG = "root_file1.jpg"
        const val FILE_TXT = "root_file2.txt"
        const val FILE_UNTYPED = "root_file3"
        const val FILE_NOT_EXIST = "12345"

        const val FILE_CONTENTS = "This is my file contents,\n info here is really important."
    }
}