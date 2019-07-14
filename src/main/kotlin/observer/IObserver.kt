package observer

import java.awt.Component
import java.awt.Dimension

interface IFileList {
    fun goBack(): IFileListResult
    fun goForward(file: String): IFileListResult
    fun getPreview(file: String = ""): IPreview
    fun getFullPath(): String
    fun getCurrentFileName(): String

    fun willDownloadHelp(file: String): Boolean
    fun downloadFile(file: String, destination: String)
}

interface IPreview {
    fun getFileList(): List<String>
    fun getDrawable(dimension: Dimension, defaultText: String = "Unable to download the preview"): Component // Task
}

data class IFileListResult(val status: Boolean, val result: IFileList)