package observer.filesystem

import observer.FileSystem
import observer.Preview
import org.apache.commons.net.ftp.FTPClient
import java.io.File


class FTPFileSystem(private val client: FTPClient) : FileSystem {
    override fun goBack(): FileSystem? {
        return if (client.changeToParentDirectory()) this else null
    }

    override fun goForward(file: String): FileSystem? {
        if (file == "..") return goBack()
        return if (client.changeWorkingDirectory(file)) this else null
    }

    override fun getPreview(file: String): Preview {
        if (isDirectory(file)) return Preview.Directory(getFileList(file))

        val inputStream = client.retrieveFileStream(file) ?: return Preview.Unhandled

        // oh god. We have to download zip to tempFile because we should invoke completePendingCommand
        // after copying or nothing will work
//        if (file.endsWith(".zip"))
//            return Preview.Remote(inputStream)

        // name of the file should be at least 3 characters length
        val tempFile = File.createTempFile("123", file)
        inputStream.use { it.copyTo(tempFile.outputStream()) }
        client.completePendingCommand()

        if (file.endsWith(".zip"))
            return Preview.Remote(tempFile.inputStream())

        return LocalFileSystem(tempFile.toPath()).getPreview("")
    }

    override fun getFileList(): List<String> {
        return getFileList("")
    }

    override fun getFullPath(): String {
        return client.printWorkingDirectory()
    }

    override fun getCurrentFileName(): String {
        return File(client.printWorkingDirectory()).name
    }

    private fun isDirectory(file: String): Boolean {
        return file == ".." || (client.changeWorkingDirectory(file) && client.changeToParentDirectory())
    }

    private fun getFileList(path: String): List<String> {
        return client.listFiles(path).map { file -> file.name }
    }

}