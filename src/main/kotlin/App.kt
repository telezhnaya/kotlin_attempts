import observer.filesystem.LocalFileSystem
import swing.MainWindow
import java.nio.file.Paths


fun main() {
    val app = MainWindow(LocalFileSystem(Paths.get("")))
    app.isVisible = true
}
