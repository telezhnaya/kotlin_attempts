import java.awt.Component
import java.io.File
import java.nio.file.Files
import java.util.*
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTextArea


class FileObserver {
    internal fun getParentandChildren(file: File): Array<File> {
        val children = file.listFiles()
        val result = mutableListOf(if (file.parentFile != null) file.parentFile else file) + children
        return result.toTypedArray()

//        val filesList = ArrayList<File>()
//        filesList.add(if (file.parentFile != null) file.parentFile else file.absoluteFile)
//        filesList.addAll(file.listFiles())
//        return filesList.toTypedArray()
    }

    fun getContents(file: File, need_parent: Boolean = true): Component {
        if (file.isDirectory) {
            return JList(if (need_parent) getParentandChildren(file) else file.listFiles())
        }

        val type = Files.probeContentType(file.toPath()) ?: ""
        // print(type + "\n")

        if (type.startsWith("image")) {
            try {
                 return JLabel(ImageIcon(ImageIO.read(file)))
            }
            catch (e: Exception) {
                // nothing to do with that, will return name of the file in the end
            }
        } else if (type.startsWith("text") || file.name.endsWith(".kt")) {
            // .kt files are not typed as text by default, and this is the main part or the project
            return JTextArea(file.readText())
        }

        return JLabel(file.absolutePath.toString())
    }
}
