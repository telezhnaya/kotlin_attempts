package swing.window

import TestBase
import observer.FileSystem.Companion.BACK
import org.fest.swing.fixture.FrameFixture
import org.junit.After
import org.junit.Before
import org.junit.Test
import swing.FILE_JLIST
import swing.NO_PREVIEW
import swing.PATH_LABEL
import swing.PREVIEW_COMPONENT
import java.awt.event.KeyEvent
import javax.swing.JTextArea


class MainWindowTest : TestBase() {
    private lateinit var window: FrameFixture

    @Before
    override fun setUp() {
        super.setUp()
        window = FrameFixture(MainWindow(fileSystem))
        window.show()
    }

    @Test
    fun `Real current path and path in label are the same`() {
        assert(window.label(PATH_LABEL).text() == rootDir.root.absolutePath)
    }

    @Test
    fun `Click on item shows corresponding preview`() {
        window.list(FILE_JLIST).item(FILE_UNTYPED).click()
        assert(window.label(PREVIEW_COMPONENT).text() == NO_PREVIEW)
    }

    @Test
    fun `Preview of text files is shown fine`() {
        // File is empty now, let's fill it with something
        val file = rootDir.root.resolve(FILE_TXT)
        file.writeText(FILE_CONTENTS)

        window.list(FILE_JLIST).item(FILE_TXT).click()
        val scrollPane = window.scrollPane(PREVIEW_COMPONENT).component()
        val textArea = scrollPane.viewport.view as JTextArea
        assert(textArea.text == FILE_CONTENTS)
    }

    @Test
    fun `Path changes to parent when left arrow is pressed`() {
        val path = window.label(PATH_LABEL).text()

        window.list(FILE_JLIST).pressAndReleaseKeys(KeyEvent.VK_LEFT)
        val parentPath = window.label(PATH_LABEL).text()

        assert(path!!.startsWith(parentPath))
        assert(path != parentPath)
    }

    @Test
    fun `File list changes when right arrow is pressed`() {
        val files = window.list(FILE_JLIST).contents()

        window.list(FILE_JLIST).selectItem(SUB_DIR)
        window.list(FILE_JLIST).pressAndReleaseKeys(KeyEvent.VK_RIGHT)
        val filesAfterClick = window.list(FILE_JLIST).contents()

        assert(!files!!.contentEquals(filesAfterClick))
    }

    @Test
    fun `File list correctly shows empty directory`() {
        this.`File list changes when right arrow is pressed`()
        val filesAfterClick = window.list(FILE_JLIST).contents()

        assert(filesAfterClick!!.contentEquals(listOf(BACK).toTypedArray()))
    }

    @After
    override fun tearDown() {
        super.tearDown()
        window.cleanUp()
    }
}