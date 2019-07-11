import java.awt.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTextArea

interface IFileList {
    fun goBack() //Task<IFileList>
    fun goForward(path: String): Boolean //Task<IFileList>
    fun getPreview(file: String): IPreview
    fun getCurrentDir(): String
}

interface IPreview {
    fun getFileList(): List<String>
    fun getDrawable(): Component
}

class LocalFileList(var curPath: Path) : IFileList {
    init {
        curPath = curPath.toAbsolutePath()
    }

    override fun goBack() {
        curPath = curPath.parent ?: curPath
    }

    override fun goForward(path: String): Boolean {
        if (curPath.resolve(path).toAbsolutePath().toFile().isDirectory) {
            curPath = curPath.resolve(path)
            return true
        }
        return false
    }

    override fun getPreview(file: String): IPreview {
        return LocalPreviewer(curPath.resolve(file))
    }

    override fun getCurrentDir(): String {
        return curPath.toString()
    }
}

class LocalPreviewer(path: Path) : IPreview {
    val path = path.toAbsolutePath()

    override fun getDrawable(): Component {
        // how to manage exceptions better?
        // we want to give last option anyway

        return when (this.getMimeType()) {
            "directory" -> JList(this.getFileList().toTypedArray())
            "image" -> JLabel(ImageIcon(ImageIO.read(this.getContents())))
            "text" -> JTextArea(this.getContents().readText())
            else -> JLabel(this.getName())
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
        if (path.endsWith(".kt")) return "text"
        if (path.toFile().isDirectory) return "directory"
        return Files.probeContentType(path)?.substringBefore('/') ?: "unknown"
    }

    private fun getContents(): File {
        return path.toFile()
    }
}
