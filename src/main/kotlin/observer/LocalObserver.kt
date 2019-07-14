package observer

import swing.getScaledDimension
import java.awt.Component
import java.awt.Dimension
import java.awt.Image
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.*

class LocalFileList(path: Path) : IFileList {
    private var fullPath = path.toAbsolutePath()

    override fun goBack(): IFileListResult {
        val status = fullPath != fullPath.parent
        fullPath = fullPath.parent ?: fullPath
        return IFileListResult(status, this)
    }

    override fun goForward(file: String): IFileListResult {
        if (file == "..") return goBack()

        val newPath = fullPath.resolve(file)
        if (file.endsWith(".zip"))
            return IFileListResult(true, ZipFileList(newPath, this))

        val isPathChanging = newPath.toFile().isDirectory
        if (isPathChanging) fullPath = newPath
        return IFileListResult(isPathChanging, this)
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

    override fun willDownloadHelp(file: String): Boolean {
        return false
    }

    override fun downloadFile(file: String, destination: String) {
        if (!File(destination).exists()) throw FileNotFoundException(destination)

        val fileToCreate = fullPath.resolve(file).toFile()
        if (fileToCreate.exists()) throw FileAlreadyExistsException(fileToCreate)
        fileToCreate.inputStream().copyTo(File(destination).outputStream())
    }
}


class LocalPreviewer(path: Path) : IPreview {
    private val path = path.toAbsolutePath()

    override fun getDrawable(dimension: Dimension, defaultText: String): Component {
        return try {
            when (getMimeType()) {
                "directory" -> JScrollPane(JList(getFileList().toTypedArray()))
                "zip" -> ZipPreviewer(path.toFile()).getDrawable(dimension, defaultText)
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
                else -> JLabel(defaultText)
            }
        } catch (e: Exception) {
            JLabel(defaultText)
        }
    }

    override fun getFileList(): List<String> {
        val files = path.toFile().listFiles() ?: listOf<File>().toTypedArray()
        return files.map { file -> file.name }
    }

    private fun getMimeType(): String {
        // main part of the whole project, probeContentType does not know about Kotlin still
        if (path.toString().endsWith(".kt") || path.toString().endsWith(".kts")) return "text"
        if (path.toString().endsWith(".zip")) return "zip" // probeContentType gives application/zip
        if (path.toFile().isDirectory) return "directory"
        return Files.probeContentType(path)?.substringBefore('/') ?: "unknown"
    }
}