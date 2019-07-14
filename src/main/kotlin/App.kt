import observer.LocalFileList
import swing.MainWindow
import java.nio.file.Paths


fun main() {
    val app = MainWindow(LocalFileList(Paths.get("")))
    app.isVisible = true
}
