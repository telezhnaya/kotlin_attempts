package swing.window

import TestBase
import TestBase.Companion.FILE_CONTENTS
import TestBase.Companion.FILE_TXT
import TestBase.Companion.FILE_UNTYPED
import TestBase.Companion.SUB_DIR
import observer.FileSystem
import observer.FileSystem.Companion.BACK
import org.fest.swing.finder.WindowFinder
import org.fest.swing.fixture.FrameFixture
import org.junit.After
import org.junit.Before
import org.junit.Test
import swing.*
import java.awt.event.KeyEvent
import javax.swing.JTextArea


class MainWindowTest {
    private val testBase = TestBase()
    private lateinit var fileSystem: FileSystem
    private lateinit var window: FrameFixture

    @Before
    fun setUp() {
        testBase.setUp()
        fileSystem = testBase.fileSystem
        window = FrameFixture(MainWindow(fileSystem))
        window.show()
    }

    @Test
    fun pathLabelShouldShowCorrectPath() {
        assert(window.label(PATH_LABEL).text() == testBase.rootDir.root.absolutePath)
    }

    @Test
    fun clickOnItemShouldShowPreview() {
        window.list(FILE_JLIST).item(FILE_UNTYPED).click()
        assert(window.label(PREVIEW_COMPONENT).text() == NO_PREVIEW)
    }

    @Test
    fun textPreviewShouldOpen() {
        // File is empty now, let's fill it with something
        val file = testBase.rootDir.root.resolve(FILE_TXT)
        file.writeText(FILE_CONTENTS)

        window.list(FILE_JLIST).item(FILE_TXT).click()
        val scrollPane = window.scrollPane(PREVIEW_COMPONENT).component()
        val textArea = scrollPane.viewport.view as JTextArea
        assert(textArea.text == FILE_CONTENTS)
    }

    @Test
    fun goBackShouldChangePathLabel() {
        val path = window.label(PATH_LABEL).text()

        window.list(FILE_JLIST).pressAndReleaseKeys(KeyEvent.VK_LEFT)
        val parentPath = window.label(PATH_LABEL).text()

        assert(path!!.startsWith(parentPath))
        assert(path != parentPath)
    }

    @Test
    fun goForwardShouldChangeFileList() {
        val files = window.list(FILE_JLIST).contents()

        window.list(FILE_JLIST).selectItem(SUB_DIR)
        window.list(FILE_JLIST).pressAndReleaseKeys(KeyEvent.VK_RIGHT)
        val filesAfterClick = window.list(FILE_JLIST).contents()

        assert(!files!!.contentEquals(filesAfterClick))
    }

    @Test
    fun fileListShowCorrectListWithBackOption() {
        this.goForwardShouldChangeFileList()
        val filesAfterClick = window.list(FILE_JLIST).contents()

        assert(filesAfterClick!!.contentEquals(listOf(BACK).toTypedArray()))
    }

    @Test
    fun cancelShouldCloseSettingsWindow() {
        window.button(FTP_SETTINGS_BUTTON).click()
        val settings = WindowFinder.findFrame(FTPSettingsWindow::class.java).using(window.robot)
        settings.requireVisible()
        settings.button(CANCEL_BUTTON).click()
        settings.requireNotVisible()
    }

    @Test
    fun anonymousCheckBoxShouldDisableUsernameLabelAndField() {
        window.button(FTP_SETTINGS_BUTTON).click()
        val settings = WindowFinder.findFrame(FTPSettingsWindow::class.java).using(window.robot)
        settings.label(USERNAME_LABEL).requireEnabled()
        settings.textBox(USERNAME_TEXT_FIELD).requireEnabled()
        settings.checkBox(ANONYMOUS_CHECKBOX).check()
        settings.label(USERNAME_LABEL).requireDisabled()
        settings.textBox(USERNAME_TEXT_FIELD).requireDisabled()
    }

    @After
    fun tearDown() {
        window.cleanUp()
        testBase.tearDown()
    }
}