import observer.LocalFileList
import java.nio.file.Paths


fun main() {

    val fileList = LocalFileList(Paths.get(""))
    //val fileList = FTPFileList(client)

    // TODO threads!!!
    val app = MainWindow(fileList)
    app.isVisible = true
}
