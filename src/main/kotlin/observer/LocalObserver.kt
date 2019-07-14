package observer

import getScaledDimension
import java.awt.Component
import java.awt.Dimension
import java.awt.Image
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.*

class LocalFileList(path: Path) : IFileList {
    private var fullPath = path.toAbsolutePath()

    override fun goBack(): IFileList {
        fullPath = fullPath.parent ?: fullPath
        return this
    }

    override fun goForward(file: String): IFileList {
        if (file == "..") return goBack()

        val newPath = fullPath.resolve(file)
        if (file.endsWith(".zip")) return ZipFileList(newPath, this)

        if (newPath.toFile().isDirectory)
            fullPath = newPath
        return this
    }

    override fun getPreview(file: String): IPreview {
        return LocalPreviewer(fullPath.resolve(file))
    }

    override fun getFullPath(): String {
        return fullPath.toString()
    }

    override fun getCurrentFileName(): String {
        return fullPath.toFile().name
    }
}

class LocalPreviewer(path: Path) : IPreview {
    private val path = path.toAbsolutePath()

    override fun getDrawable(dimension: Dimension): Component {
        return try {
            when (getMimeType()) {
                "directory" -> JScrollPane(JList(getFileList().toTypedArray()))
                "zip" -> ZipPreviewer(path.toFile()).getDrawable(dimension)
                "image" -> {
                    val img = ImageIO.read(path.toFile())
                    val imgDimension = getScaledDimension(Dimension(img.width, img.height), dimension)
                    JLabel(
                        ImageIcon(
                            img.getScaledInstance(
                                imgDimension.width,
                                imgDimension.height,
                                Image.SCALE_SMOOTH
                            )
                        )
                    )
                }
                "text" -> JScrollPane(JTextArea(path.toFile().readText()))
                else -> JScrollPane(JLabel(path.toString()))
            }
        } catch (e: Exception) {
            JScrollPane(JLabel(path.toString()))
        }
    }

    override fun getFileList(): List<String> {
        val files = path.toFile().listFiles() ?: listOf<File>().toTypedArray()
        return files.map { file -> file.name }
    }

    private fun getMimeType(): String {
        if (path.toString().endsWith(".kt")) return "text"
        if (path.toString().endsWith(".zip")) return "zip" // probeContentType gives application/zip
        if (path.toFile().isDirectory) return "directory"
        return Files.probeContentType(path)?.substringBefore('/') ?: "unknown"
    }
}