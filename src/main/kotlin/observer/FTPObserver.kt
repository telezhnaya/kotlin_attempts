package observer

import org.apache.commons.net.ftp.FTPClient
import java.awt.Component
import java.awt.Dimension
import java.io.File
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JScrollPane


// TODO support zip
class FTPFileList(val client: FTPClient, lazyLoad: Boolean = true) : IFileList {
    override fun goBack(): IFileList {
        client.changeToParentDirectory()
        return this
    }

    override fun goForward(path: String): IFileList {
        if (path == "..") return goBack()
        client.changeWorkingDirectory(path)
        return this
    }

    override fun getPreview(file: String): IPreview {
        return FTPPreviewer(client, file)
    }

    override fun getCurrentDir(): String {
        return client.printWorkingDirectory()
    }
}

class FTPPreviewer(private val client: FTPClient, private val path: String) : IPreview {
    override fun getDrawable(dimension: Dimension): Component {
        if (isDirectory()) return JScrollPane(JList(getFileList().toTypedArray()))

        return try {
            val file = File.createTempFile("morethanthree", path)
            client.retrieveFileStream(path).copyTo(file.outputStream())
            // why it was not anywhere except chinese site on 3rd google page???
            client.completePendingCommand() // will not work without this line
            LocalPreviewer(file.toPath()).getDrawable(dimension)
        } catch (e: Exception) {
            // whatever will go wrong, we can't do anything with that
            JScrollPane(JLabel(path))
        }
    }

    override fun getFileList(): List<String> {
        return client.listFiles(path).map { file -> file.name }
    }

    private fun isDirectory(): Boolean {
        return path == ".." || (client.changeWorkingDirectory(path) && client.changeToParentDirectory())
    }
}