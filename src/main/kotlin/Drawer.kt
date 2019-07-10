import java.awt.Component
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTextArea

interface IDrawable {
    fun getDrawable(preview: IPreview): Component
}

class Drawer : IDrawable {
    override fun getDrawable(preview: IPreview): Component {
        // how to manage exceptions better?
        // we want to give last option anyway

        return when (preview.getMimeType()) {
            "directory" -> JList(preview.getParentObject().getFileList().toTypedArray())
            "image" -> JLabel(ImageIcon(ImageIO.read(preview.getContents())))
            "text" -> JTextArea(preview.getContents().readText())
            else -> JLabel(preview.getName())
        }
    }
}
