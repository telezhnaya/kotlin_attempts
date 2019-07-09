import java.nio.file.Paths

fun main() {
    // TODO threads!!!
    val app = MainWindow(Paths.get("").toAbsolutePath().toFile())
    app.isVisible = true
}
