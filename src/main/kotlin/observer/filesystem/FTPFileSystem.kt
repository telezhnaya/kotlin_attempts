package observer.filesystem

import observer.FileSystem
import observer.Preview
import observer.preview.FTPPreview
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileNotFoundException


class FTPFileSystem(private val client: FTPClient) : FileSystem {
    override fun goBack(): FileSystem? {
        return if (client.changeToParentDirectory()) this else null
    }

    override fun goForward(file: String): FileSystem? {
        if (file == "..") return goBack()
        return if (client.changeWorkingDirectory(file)) this else null
    }

    override fun getPreview(file: String): Preview {
        return FTPPreview(client, file)
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