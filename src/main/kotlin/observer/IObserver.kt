package observer

import java.awt.Component
import java.awt.Dimension

interface IFileList {
    fun goBack(): IFileList //Task<observer.IFileList>
    fun goForward(path: String): IFileList //Task<observer.IFileList>
    fun getPreview(file: String): IPreview
    fun getCurrentDir(): String
}

interface IPreview {
    fun getFileList(): List<String>
    fun getDrawable(dimension: Dimension): Component
}
