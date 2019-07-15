package observer.filesystem

import observer.FileSystem
import observer.Preview
import observer.preview.FTPPreview
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
        return FTPPreview(client, file)
    }

    override fun getFullPath(): String {
        return client.printWorkingDirectory()
    }

    override fun getCurrentFileName(): String {
        return File(client.printWorkingDirectory()).name
    }
}