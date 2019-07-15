package observer.preview

import observer.Preview
import swing.scale
import java.awt.Component
import java.awt.Dimension
import java.awt.Image
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.*


class LocalPreview(path: Path) : Preview {
    private val path = path.toAbsolutePath()

    override fun getDrawable(dimension: Dimension, defaultText: String): Component {
        return try {
            when (getMimeType()) {
                "directory" -> JScrollPane(JList(getFileList().toTypedArray()))
                "zip" -> ZipPreview(path.toFile()).getDrawable(dimension, defaultText)
                "image" -> {
                    val img = ImageIO.read(path.toFile())
                    val imgDimension = Dimension(img.width, img.height).scale(dimension)
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