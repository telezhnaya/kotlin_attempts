package observer

import java.awt.Component
import java.awt.Dimension
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipFile
import javax.swing.JList
import javax.swing.JScrollPane


// TODO check for empty archives, archives in archives
class ZipFileList(zipPath: Path, private val parent: IFileList) : IFileList {
    private val zipFile = ZipFile(zipPath.toFile())
    private var currentPath = ""

    override fun goBack(): IFileList {
        if (File(currentPath).toPath().nameCount == 1) return parent
        currentPath = File(currentPath).parent
        return this
    }

    override fun goForward(path: String): IFileList {
        if (path == "..") return goBack()
        val newPath = File(currentPath).resolve(path).toString()
        if (zipFile.getEntry(newPath) != null) currentPath = newPath
        return this
    }

    override fun getPreview(file: String): IPreview {
        val path = File(currentPath).resolve(file).normalize().toString()
        return if (path != "..") ZipPreviewer(zipFile, path) else parent.getPreview("")
    }

    override fun getCurrentDir(): String {
        return File(currentPath).absolutePath
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

    override fun getDrawable(dimension: Dimension): Component {
        if (isDirectory())
            return JScrollPane(JList(getFileList().toTypedArray()))
        val entry = zipFile.getEntry(path)
        val f = File.createTempFile("morethanthree", File(entry.name).name)
        zipFile.getInputStream(entry).copyTo(f.outputStream())
        return LocalPreviewer(f.toPath()).getDrawable(dimension)
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
        return path == "" || zipFile.getEntry(path).isDirectory
    }
}