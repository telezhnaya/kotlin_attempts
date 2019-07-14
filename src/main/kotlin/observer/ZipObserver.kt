package observer

import java.awt.Component
import java.awt.Dimension
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.zip.ZipFile
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JScrollPane

class ZipFileList(zipPath: Path, private val parent: IFileList) : IFileList {
    private val zipFile = ZipFile(zipPath.toFile())
    private val zipName = zipPath.fileName.toString()
    private var currentPath = ""

    override fun goBack(): IFileListResult {
        if (File(currentPath).toPath().nameCount == 1) return IFileListResult(true, parent)
        currentPath = File(currentPath).parent
        return IFileListResult(true, this)
    }

    override fun goForward(file: String): IFileListResult {
        if (file == "..") return goBack()
        val newPath = getPath(file)

        val isPathChanging = zipFile.getEntry(newPath)?.isDirectory == true
        if (isPathChanging) currentPath = newPath
        return IFileListResult(isPathChanging, this)
    }

    override fun getPreview(file: String): IPreview {
        val path = getPath(file)
        return if (path != "..") ZipPreviewer(zipFile, path) else parent.getPreview("")
    }

    override fun getFullPath(): String {
        return File(zipFile.name).resolve(currentPath).path
    }

    override fun getCurrentFileName(): String {
        return if (currentPath.isEmpty()) zipName else File(currentPath).name
    }

    override fun willDownloadHelp(file: String): Boolean {
        return zipFile.getEntry(getPath(file)) != null && file.endsWith(".zip")
    }

    override fun downloadFile(file: String, destination: String) {
        if (!File(destination).exists()) throw FileNotFoundException(destination)

        val fileToCreate = File(destination).resolve(file)
        if (fileToCreate.exists()) throw FileAlreadyExistsException(fileToCreate)

        val entry = zipFile.getEntry(getPath(file))
        zipFile.getInputStream(entry).copyTo(fileToCreate.outputStream())
    }

    private fun getPath(file: String): String {
        return File(currentPath).resolve(file).normalize().toString()
    }
}


class ZipPreviewer : IPreview {
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
            LocalPreviewer(f.toPath()).getDrawable(dimension, defaultText)
        } catch (e: Exception) {
            JLabel(defaultText)
        }
    }

    override fun getFileList(): List<String> {
        val depth = if (path.isEmpty()) 1 else File(path).toPath().nameCount + 1
        val namesList = mutableListOf<String>()
        for (e in zipFile.entries()) {
            if (e.name.startsWith(path) && File(e.name).toPath().nameCount == depth)
                namesList.add(File(e.name).name)
        }
        return namesList
    }

    private fun isDirectory(): Boolean {
        return path.isEmpty() || zipFile.getEntry(path).isDirectory
    }
}