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
    return try {
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