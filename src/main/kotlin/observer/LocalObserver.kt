package observer

import java.awt.Component
import java.awt.Dimension
import java.awt.Image
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.*

class LocalFileList(private var curPath: Path) : IFileList {
    init {
        curPath = curPath.toAbsolutePath()
    }

    override fun goBack(): IFileList {
        curPath = curPath.parent ?: curPath
        return this
    }

    override fun goForward(path: String): IFileList {
        if (path == "..") return goBack()
        if (path.endsWith(".zip"))
            return ZipFileList(curPath.resolve(path), this)
        if (curPath.resolve(path).toAbsolutePath().toFile().isDirectory)
            curPath = curPath.resolve(path)
        return this
    }

    override fun getPreview(file: String): IPreview {
        return LocalPreviewer(curPath.resolve(file))
    }

    override fun getFullPath(): String {
        return curPath.toString()
    }

    override fun getCurrentFileName(): String {
        return curPath.toFile().name
    }
}

class LocalPreviewer(path: Path) : IPreview {
    private val path = path.toAbsolutePath()

    override fun getDrawable(dimension: Dimension): Component {
        // how to manage exceptions better?
        // we want to give last option anyway
        val a = getMimeType()
        return when (a) {
            "directory" -> JScrollPane(JList(getFileList().toTypedArray()))
            "zip" -> ZipPreviewer(path.toFile()).getDrawable(dimension)
            "image" -> {
                val img = ImageIO.read(getContents())
                val imgDimension = getScaledDimension(Dimension(img.width, img.height), dimension)
                JLabel(ImageIcon(img.getScaledInstance(imgDimension.width, imgDimension.height, Image.SCALE_SMOOTH)))
            }
            "text" -> JScrollPane(JTextArea(getContents().readText()))
            else -> JScrollPane(JLabel(getName()))
        }
    }

    override fun getFileList(): List<String> {
        val files = path.toFile().listFiles() ?: listOf<File>().toTypedArray()
        return files.map { file -> file.name }
    }

    private fun getName(): String {
        return path.toString()
    }

    private fun getMimeType(): String {
        if (path.toString().endsWith(".kt")) return "text"
        if (path.toString().endsWith(".zip")) return "zip" // probeContentType gives application/zip
        if (path.toFile().isDirectory) return "directory"
        return Files.probeContentType(path)?.substringBefore('/') ?: "unknown"
    }

    private fun getContents(): File {
        return path.toFile()
    }

    private fun getScaledDimension(img: Dimension, boundary: Dimension): Dimension {
        val widthRatio = boundary.getWidth() / img.getWidth()
        val heightRatio = boundary.getHeight() / img.getHeight()
        val ratio = Math.min(widthRatio, heightRatio)

        return Dimension((img.width * ratio).toInt(), (img.height * ratio).toInt())
    }
}