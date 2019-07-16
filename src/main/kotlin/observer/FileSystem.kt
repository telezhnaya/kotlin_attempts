package observer

interface FileSystem {
    fun goBack(): FileSystem?
    fun goForward(file: String): FileSystem?

    fun getPreview(file: String = ""): Preview
    fun getFileList(): List<String>

    fun getFullPath(): String
    fun getCurrentFileName(): String
}