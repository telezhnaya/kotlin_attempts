import observer.FTPFileList
import org.apache.commons.net.ftp.FTPClient


fun main() {

    val client = FTPClient()

    client.connect(server)
    client.login(user, pass)

    //val fileList = LocalFileList(Paths.get(""))
    val fileList = FTPFileList(client)

    // TODO threads!!!
    val app = MainWindow(fileList)
    app.isVisible = true
}
