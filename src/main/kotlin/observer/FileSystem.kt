package observer

interface FileSystem {
    fun goBack(): FileSystem?
    fun goForward(file: String): FileSystem?

    fun getPreview(file: String = ""): Preview
    fun getFullPath(): String
    fun getCurrentFileName(): String

    fun willDownloadHelp(file: String): Boolean
    fun downloadFile(file: String, destination: String) // Task
}