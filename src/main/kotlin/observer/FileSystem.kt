package observer

interface FileSystem {
    suspend fun goBack(): FileSystem?
    suspend fun goForward(file: String): FileSystem?

    suspend fun getPreview(file: String = ""): Preview
    suspend fun getFileList(): List<String>

    fun getFullPath(): String
    fun getCurrentFileName(): String
}