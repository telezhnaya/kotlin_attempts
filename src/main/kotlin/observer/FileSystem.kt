package observer

interface FileSystem {
    fun goBack(): FileSystem?
    fun goForward(file: String): FileSystem?

    fun getPreview(file: String = ""): PreviewData
    fun getFileList(): List<String>

    fun getFullPath(): String
    fun getCurrentFileName(): String

    companion object {
        const val BACK = ".."
        const val ZIP_EXTENSION = ".zip"
    }
}