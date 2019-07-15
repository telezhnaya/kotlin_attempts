package observer.preview

import observer.Preview
import java.awt.Component
import java.awt.Dimension
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JScrollPane


class ZipPreview : Preview {
    private val zipFile: ZipFile
    private val path: String

    constructor(file: File) {
        zipFile = ZipFile(file)
        path = ""
    }

    constructor(zip: ZipFile, addPath: String) {
        zipFile = zip
        path = addPath
    }

    override fun getDrawable(dimension: Dimension, defaultText: String): Component {
        if (isDirectory())
            return JScrollPane(JList(getFileList().toTypedArray()))

        if (path.endsWith(".zip"))
            return JLabel(defaultText)

        return try {
            val entry = zipFile.getEntry(path)
            // name of the file should be at least 3 characters length
            val f = File.createTempFile("1234", File(entry.name).name)
            zipFile.getInputStream(entry).copyTo(f.outputStream())
            LocalPreview(f.toPath()).getDrawable(dimension, defaultText)
        } catch (e: Exception) {
            JLabel(defaultText)
        }
    }

    override fun getFileList(): List<String> {
        val depth = if (path.isEmpty()) 1 else File(path).toPath().nameCount + 1

        fun isFileEntryInPath(e: ZipEntry): Boolean {
            return e.name.startsWith(path) && File(e.name).toPath().nameCount == depth
        }

        return zipFile.entries().toList()
            .filter { isFileEntryInPath(it) }
            .map { File(it.name).name }
    }

    override fun willDownloadHelp(): Boolean {
        return zipFile.getEntry(path) != null && path.endsWith(".zip")
    }

    override fun downloadFile(destination: Path) {
        val destinationFile = destination.toFile()
        if (!destinationFile.exists()) throw FileNotFoundException(destination.toString())

        val fileToCreate = destinationFile.resolve(File(path).name)
        if (fileToCreate.exists()) throw FileAlreadyExistsException(fileToCreate)

        val entry = zipFile.getEntry(path)
        zipFile.getInputStream(entry).copyTo(fileToCreate.outputStream())
    }

    private fun isDirectory(): Boolean {
        return path.isEmpty() || zipFile.getEntry(path).isDirectory
    }
}