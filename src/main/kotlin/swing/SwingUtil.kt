package swing

import observer.FileSystem.Companion.BACK
import observer.PreviewData
import java.awt.*
import java.io.BufferedReader
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.min


const val CANCEL = "Cancel"
const val SUBMIT = "Submit"
const val NO_PREVIEW = "Preview is not supported yet"
const val WINDOW_OFFSET = 50 // in pixels

fun getComponent(previewData: PreviewData, dimension: Dimension): Component {
    val component: Component = try {
        when (previewData) {
            is PreviewData.Directory -> JScrollPane(JList(previewData.paths.toTypedArray()))
            is PreviewData.Image -> {
                val img = previewData.inputStream.use { ImageIO.read(it) }
                val imgDimension = Dimension(img.width, img.height).scale(dimension)
                JLabel(ImageIcon(img.getScaledInstance(imgDimension.width, imgDimension.height, Image.SCALE_SMOOTH)))
            }
            is PreviewData.Text -> {
                val text = previewData.inputStream.bufferedReader().use(BufferedReader::readText)
                JScrollPane(JTextArea(text))
            }
            is PreviewData.Remote -> JLabel("Click Enter to extract this file")
            is PreviewData.Unhandled -> JLabel(NO_PREVIEW)
        }
    } catch (e: Exception) {
        JLabel(NO_PREVIEW)
    }
    component.name = PREVIEW_COMPONENT
    return component
}

fun Dimension.scale(boundary: Dimension): Dimension {
    val widthRatio = boundary.getWidth() / this.width
    val heightRatio = boundary.getHeight() / this.height
    val ratio = min(widthRatio, heightRatio)
    return Dimension((this.width * ratio).toInt(), (this.height * ratio).toInt())
}

fun JLabel.reloadText(text: String) {
    this.text = text
    this.revalidate()
    this.repaint()
}

fun JTextArea.reloadText(text: String) {
    this.text = text
    this.revalidate()
    this.repaint()
}

fun JTextArea.initErrorField() {
    this.foreground = Color.RED
    this.lineWrap = true
    this.isEditable = false
    this.isOpaque = false
    this.wrapStyleWord = true
}

fun DefaultListModel<String>.refill(files: List<String>) {
    this.removeAllElements()
    this.add(0, BACK)
    this.addAll(files)
}

fun JPanel.reloadElement(element: Component, position: Int) {
    this.remove(position)
    this.add(element, position)
    this.revalidate()
    this.repaint()
}

fun createGridBagConstraints(
    gridx: Int,
    gridy: Int,
    weightx: Double,
    weighty: Double,
    gridwidth: Int = 1
): GridBagConstraints {
    return GridBagConstraints(
        gridx,
        gridy,
        gridwidth,
        1,
        weightx,
        weighty,
        10,
        GridBagConstraints.BOTH,
        Insets(0, 0, 0, 0),
        0,
        0
    )
}

// ------- names of swing objects for testing purposes -------
const val PATH_LABEL = "full path label"
const val FTP_SETTINGS_BUTTON = "FTP settings button"
const val FILE_JLIST = "List of all directories and files"
const val PREVIEW_COMPONENT = "Component with the preview (type could be different)"
const val ANONYMOUS_CHECKBOX = "Anonymous checkbox"
const val CANCEL_BUTTON = "Cancel button"
const val USERNAME_LABEL = "Username label"
const val USERNAME_TEXT_FIELD = "Username text field"