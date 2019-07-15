package swing

import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.min


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