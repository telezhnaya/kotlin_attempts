package observer

import java.awt.Component
import java.awt.Dimension
import java.nio.file.Path

interface Preview {
    fun willDownloadHelp(): Boolean
    fun downloadFile(destination: Path) // Task

    fun getFileList(): List<String>
    fun getDrawable(dimension: Dimension, defaultText: String = "Unable to download the preview"): Component // Task
}