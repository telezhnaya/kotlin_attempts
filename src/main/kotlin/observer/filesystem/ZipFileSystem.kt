package observer.filesystem

import observer.FileSystem
import observer.Preview
import observer.preview.ZipPreview
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipFile

class ZipFileSystem(zipPath: Path, private val parent: FileSystem) : FileSystem {
    private val zipFile = ZipFile(zipPath.toFile())
    private val zipName = zipPath.fileName.toString()
    private var currentPath = ""

    override fun goBack(): FileSystem? {
        if (currentPath.isEmpty()) return parent
        currentPath = File(currentPath).parent ?: ""
        return this
    }

    override fun goForward(file: String): FileSystem? {
        if (file == "..") return goBack()
        val newPath = getPath(file)

        val isNewPath = zipFile.getEntry(newPath)?.isDirectory == true
        if (isNewPath) currentPath = newPath
        return if (isNewPath) this else null
    }

    override fun getPreview(file: String): Preview {
        val path = getPath(file)
        return if (path != "..") ZipPreview(zipFile, path) else parent.getPreview("")
    }

    override fun getFullPath(): String {
        return File(zipFile.name).resolve(currentPath).path
    }

    override fun getCurrentFileName(): String {
        return if (currentPath.isEmpty()) zipName else File(currentPath).name
    }

    private fun getPath(file: String): String {
        return File(currentPath).resolve(file).normalize().toString()
    }
}