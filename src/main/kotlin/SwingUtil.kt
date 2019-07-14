import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.Insets

fun getScaledDimension(inner: Dimension, boundary: Dimension): Dimension {
    val widthRatio = boundary.getWidth() / inner.getWidth()
    val heightRatio = boundary.getHeight() / inner.getHeight()
    val ratio = Math.min(widthRatio, heightRatio)

    return Dimension((inner.width * ratio).toInt(), (inner.height * ratio).toInt())
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