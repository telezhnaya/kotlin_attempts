package observer

import org.apache.commons.net.ftp.FTPClient
import java.awt.Component
import java.awt.Dimension
import java.io.File
import java.io.FileNotFoundException
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JScrollPane


class FTPFileList(private val client: FTPClient) : IFileList {
    override fun goBack(): IFileListResult {
        return IFileListResult(client.changeToParentDirectory(), this)
    }

    override fun goForward(file: String): IFileListResult {
        if (file == "..") return goBack()
        return IFileListResult(client.changeWorkingDirectory(file), this)
    }

    override fun getPreview(file: String): IPreview {
        return FTPPreviewer(client, file)
    }

    override fun getFullPath(): String {
        return client.printWorkingDirectory()
    }

    override fun getCurrentFileName(): String {
        return File(client.printWorkingDirectory()).name
    }

    override fun willDownloadHelp(file: String): Boolean {
        return client.listFiles(file).size == 1 && file.endsWith(".zip")
    }

    override fun downloadFile(file: String, destination: String) {
        if (!File(destination).exists()) throw FileNotFoundException(destination)

        val fileToCreate = File(destination).resolve(file)
        if (fileToCreate.exists()) throw FileAlreadyExistsException(fileToCreate)

        client.retrieveFileStream(file).copyTo(fileToCreate.outputStream())
        client.completePendingCommand()
    }
}


class FTPPreviewer(private val client: FTPClient, private val localPath: String) : IPreview {
    override fun getDrawable(dimension: Dimension, defaultText: String): Component {
        if (isDirectory())
            return JScrollPane(JList(getFileList().toTypedArray()))

        if (localPath.endsWith(".zip"))
            return JLabel(defaultText)

        return try {
            // name of the file should be at least 3 characters length
            val file = File.createTempFile("1234", localPath)
            client.retrieveFileStream(localPath).copyTo(file.outputStream())
            client.completePendingCommand()
            LocalPreviewer(file.toPath()).getDrawable(dimension, defaultText)
        } catch (e: Exception) {
            JLabel(defaultText)
        }
    }

    override fun getFileList(): List<String> {
        return client.listFiles(localPath).map { file -> file.name }
    }

    private fun isDirectory(): Boolean {
        return localPath == ".." || (client.changeWorkingDirectory(localPath) && client.changeToParentDirectory())
    }
}