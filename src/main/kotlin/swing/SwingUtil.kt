package swing

import observer.Preview
import java.awt.*
import java.io.BufferedReader
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.min


fun getComponent(preview: Preview, dimension: Dimension): Component {
    return try {
        when (preview) {
            is Preview.Directory -> JScrollPane(JList(preview.paths.toTypedArray()))
            is Preview.Image -> {
                val img = preview.inputStream.use { ImageIO.read(it) }
                val imgDimension = Dimension(img.width, img.height).scale(dimension)
                JLabel(ImageIcon(img.getScaledInstance(imgDimension.width, imgDimension.height, Image.SCALE_SMOOTH)))
            }
            is Preview.Text -> {
                val text = preview.inputStream.bufferedReader().use(BufferedReader::readText)
                JScrollPane(JTextArea(text))
            }
            is Preview.Remote -> JLabel("Click Enter to extract this file")
            is Preview.Unhandled -> JLabel("Preview is not supported yet")
        }
    } catch (e: Exception) {
        JLabel("Preview is not supported yet")
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

fun DefaultListModel<String>.refill(files: List<String>) {
    this.removeAllElements()
    this.add(0, "..")
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