package swing.window

import TestBase
import org.fest.swing.finder.WindowFinder
import org.fest.swing.fixture.FrameFixture
import org.junit.After
import org.junit.Before
import org.junit.Test
import swing.*


class FTPSettingsWindowTest : TestBase() {
    private lateinit var window: FrameFixture
    private lateinit var settings: FrameFixture

    @Before
    override fun setUp() {
        super.setUp()
        window = FrameFixture(MainWindow(fileSystem))
        window.show()
        window.button(FTP_SETTINGS_BUTTON).click()
        settings = WindowFinder.findFrame(FTPSettingsWindow::class.java).using(window.robot)
    }

    @Test
    fun `Pressing Cancel button should close settings window`() {
        settings.requireVisible()
        settings.button(CANCEL_BUTTON).click()
        settings.requireNotVisible()
    }

    @Test
    fun `Username field and label are disabled after checking anonymous CheckBox`() {
        settings.label(USERNAME_LABEL).requireEnabled()
        settings.textBox(USERNAME_TEXT_FIELD).requireEnabled()
        settings.checkBox(ANONYMOUS_CHECKBOX).check()
        settings.label(USERNAME_LABEL).requireDisabled()
        settings.textBox(USERNAME_TEXT_FIELD).requireDisabled()
    }

    @Test
    fun `Settings window does not close and shows error after submitting empty input`() {
        settings.button(SUBMIT_BUTTON).click()
        settings.requireVisible()
        assert(settings.textBox(SETTINGS_ERROR_LABEL).text().isNotEmpty())
    }

    @After
    override fun tearDown() {
        super.tearDown()
        settings.cleanUp()
        window.cleanUp()
    }
}