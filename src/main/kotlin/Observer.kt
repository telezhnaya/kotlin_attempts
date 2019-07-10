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
    fun getFirst(): String
}

interface IPreview {
    fun getMimeType(): String
    fun getContents(): File
    fun getFileList(): List<String>
    fun getName(): String
    fun getDrawable() : Component
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

    override fun getFirst(): String {
        return curPath.toFile().listFiles()[0].toString()
    }
}

class LocalPreviewer(path: Path) : IPreview {
    val path = path.toAbsolutePath()

    override fun getName(): String {
        return path.toString()
    }

    override fun getFileList(): List<String> {
        return path.toFile().listFiles().map { file -> file.name }
    }

    override fun getMimeType(): String {
        if (path.endsWith(".kt")) return "text"
        if (path.toFile().isDirectory) return "directory"
        return Files.probeContentType(path)?.substringBefore('/') ?: "unknown"
    }

    override fun getContents(): File {
        return path.toFile()
    }

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
}
