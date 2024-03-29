package observer.filesystem

import observer.FileSystem
import observer.FileSystem.Companion.BACK
import observer.FileSystem.Companion.ZIP_EXTENSION
import observer.PreviewData
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipFileSystem(zip: File, private val parent: FileSystem) : FileSystem {
    private val zipFile = ZipFile(zip)
    private val zipName = zip.name
    private var currentPath = ""

    override fun goBack(): FileSystem? {
        if (currentPath.isEmpty()) return parent
        currentPath = File(currentPath).parent ?: ""
        return this
    }

    override fun goForward(file: String): FileSystem? {
        if (file == BACK) return goBack()
        val newFile = File(currentPath).resolve(file).normalize()

        val isDirectory = zipFile.getEntry(newFile.path)?.isDirectory == true
        if (isDirectory) currentPath = newFile.path
        return if (isDirectory) this else null
    }

    override fun getPreview(file: String): PreviewData {
        val preview = File(currentPath).resolve(file).normalize()
        if (preview.name == BACK) return parent.getPreview("")

        val entry = zipFile.getEntry(preview.path) ?: return PreviewData.Unhandled
        if (preview.name.isEmpty() || entry.isDirectory)
            return PreviewData.Directory(getFileList(preview.path))

        val inputStream = zipFile.getInputStream(entry) ?: return PreviewData.Unhandled
        if (preview.name.endsWith(ZIP_EXTENSION))
            return PreviewData.Remote(inputStream)

        // name of the file should be at least 3 characters length
        val tempFile = File.createTempFile("123", preview.name)
        inputStream.use { it.copyTo(tempFile.outputStream()) }
        return LocalFileSystem(tempFile.toPath()).getPreview("")
    }

    override fun getFileList(): List<String> {
        return getFileList(currentPath)
    }

    override fun getFullPath(): String {
        return File(zipFile.name).resolve(currentPath).path
    }

    override fun getCurrentFileName(): String {
        return if (currentPath.isEmpty()) zipName else File(currentPath).name
    }

    private fun getFileList(path: String): List<String> {
        val depth = if (path.isEmpty()) 1 else File(path).toPath().nameCount + 1

        fun isFileEntryInPath(e: ZipEntry): Boolean {
            return e.name.startsWith(path) && File(e.name).toPath().nameCount == depth
        }

        return zipFile.entries().toList()
            .filter { isFileEntryInPath(it) }
            .sortedWith(compareBy<ZipEntry> { !it.isDirectory }.thenBy { it.name })
            .map { File(it.name).name }
    }
}