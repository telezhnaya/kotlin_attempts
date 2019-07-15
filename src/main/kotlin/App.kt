import observer.filesystem.LocalFileSystem
import swing.window.MainWindow
import java.nio.file.Paths


fun main() {
    val app = MainWindow(LocalFileSystem(Paths.get("")))
    app.isVisible = true
}
