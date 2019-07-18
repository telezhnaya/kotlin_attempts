package swing.window

import observer.filesystem.LocalFileSystem
import org.fest.swing.fixture.FrameFixture
import org.junit.Before
import org.junit.Test
import java.nio.file.Paths


internal class MainWindowTest {

    private val window = FrameFixture(MainWindow(LocalFileSystem(Paths.get(""))))

    @Before
    fun setUp() {
        window.show()
    }

    @Test
    fun pathShouldNotBeEmpty() {
        assert(window.label("currentPath").text().isNotEmpty())
    }

}