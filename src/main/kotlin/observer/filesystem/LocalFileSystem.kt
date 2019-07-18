package observer.filesystem

import observer.FileSystem
import observer.Preview
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LocalFileSystem(path: Path) : FileSystem {
    private var fullPath = path.toAbsolutePath()

    override fun goBack(): FileSystem? {
        val isNewPath = fullPath != null && fullPath != fullPath.parent
        fullPath = fullPath?.parent
        return if (isNewPath) this else null
    }

    override fun goForward(file: String): FileSystem? {
        if (file == "..") return goBack()

        val newPath = fullPath?.resolve(file) ?: Paths.get(file)
        if (file.endsWith(".zip"))
            return ZipFileSystem(newPath.toFile(), this)

        val isNewPath = newPath.toFile().isDirectory
        if (isNewPath) fullPath = newPath
        return if (isNewPath) this else null
    }

    override fun getPreview(file: String): Preview {
        if (fullPath == null) return Preview.Directory(getFileList())
        val preview = fullPath.resolve(file).toFile() ?: return Preview.Unhandled

        return when (getContentType(preview)) {
            "directory" -> Preview.Directory(getFileList(preview.toPath()))
            "image" -> Preview.Image(preview.inputStream())
            "text" -> Preview.Text(preview.inputStream())
            "zip" -> ZipFileSystem(preview, this).getPreview("")
            else -> Preview.Unhandled
        }
    }

    override fun getFileList(): List<String> {
        return getFileList(fullPath)
    }

    override fun getFullPath(): String {
        return fullPath?.toString() ?: ""
    }

    override fun getCurrentFileName(): String {
        return fullPath?.toFile()?.name ?: ""
    }

    private fun getFileList(path: Path?): List<String> {
        if (path == null) return File.listRoots().map { file -> file.path }

        val files = path.toFile().listFiles() ?: listOf<File>().toTypedArray()
        return files
            .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })
            .map { file -> file.name }
    }

    private fun getContentType(file: File): String {
        // main part of the whole project, probeContentType does not know about Kotlin still
        if (file.name.endsWith(".kt") || file.name.endsWith(".kts")) return "text"
        if (file.isDirectory) return "directory"
        if (file.name.endsWith(".zip")) return "zip" // probeContentType gives application/zip
        return Files.probeContentType(file.toPath())?.substringBefore('/') ?: "unknown"
    }
}