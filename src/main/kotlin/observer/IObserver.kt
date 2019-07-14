package observer

import java.awt.Component
import java.awt.Dimension

interface IFileList {
    fun goBack(): IFileList
    fun goForward(file: String): IFileList
    fun getPreview(file: String): IPreview
    fun getFullPath(): String
    fun getCurrentFileName(): String
}

interface IPreview {
    fun getFileList(): List<String>
    fun getDrawable(dimension: Dimension, defaultText: String = "Unable to download the preview"): Component // Task
}
