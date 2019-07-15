package observer

import java.awt.Component
import java.awt.Dimension

interface Preview {
    fun getFileList(): List<String>
    fun getDrawable(dimension: Dimension, defaultText: String = "Unable to download the preview"): Component // Task
}