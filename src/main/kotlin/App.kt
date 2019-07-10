import java.nio.file.Paths
import org.apache.commons.net.ftp.FTPClient
import java.nio.file.Files

fun main() {

    val fileList : IFileList = LocalFileList(Paths.get(""))

    // TODO threads!!!
    val app = MainWindow(fileList)
    app.isVisible = true
}
