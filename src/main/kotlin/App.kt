import java.nio.file.Paths

fun main() {

    val fileList = LocalFileList(Paths.get(""))

    // TODO threads!!!
    val app = MainWindow(fileList)
    app.isVisible = true
}
