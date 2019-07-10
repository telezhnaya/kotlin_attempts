import java.nio.file.Paths
import org.apache.commons.net.ftp.FTPClient

fun main() {

    // TODO threads!!!
    val app = MainWindow(Paths.get("").toAbsolutePath().toFile())
    app.isVisible = true
}
