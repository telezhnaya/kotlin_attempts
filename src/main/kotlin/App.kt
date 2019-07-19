import observer.filesystem.LocalFileSystem
import swing.window.MainWindow
import java.nio.file.Paths
import javax.swing.SwingUtilities


fun main() {
    SwingUtilities.invokeAndWait {
        val app = MainWindow(LocalFileSystem(Paths.get("")))
        app.isVisible = true
    }
}
