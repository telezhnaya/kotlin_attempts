import java.io.File
import java.nio.file.Files
import java.nio.file.Path

interface IFileList {
    fun goBack() //Task<IFileList>
    fun goForward(path: String): Boolean //Task<IFileList>
    fun getPreview(file: String): IPreview
    fun getCurrentDir(): String
    fun getFirst(): String
}

interface IPreview {
    fun getMimeType(): String
    fun getContents(): File
    fun getFileList(): List<String>
    fun getParentObject(): IFileList
    fun getName(): String
}

class LocalFileList(var curPath: Path) : IFileList {
    init {
        curPath = curPath.toAbsolutePath()
    }

    override fun goBack() {
        curPath = curPath.parent ?: curPath
    }

    override fun goForward(path: String): Boolean {
        if (curPath.resolve(path).toAbsolutePath().toFile().isDirectory) {
            curPath = curPath.resolve(path)
            return true
        }
        return false
    }

    override fun getPreview(file: String): IPreview {
        return LocalPreviewer(this, curPath.resolve(file))
    }

    override fun getCurrentDir(): String {
        return curPath.toString()
    }

    override fun getFirst(): String {
        return curPath.toFile().listFiles()[0].toString()
    }
}

class LocalPreviewer(val parent: IFileList, val path: Path) : IPreview {
//    init {
//        path = path.toAbsolutePath()
//    }

    override fun getName(): String {
        return path.toAbsolutePath().toString()
    }

    override fun getFileList(): List<String> {
        return path.toAbsolutePath().toFile().listFiles().map { file -> file.name }
    }

    // can't name it getParent, JVM gives an error "method exists"
    override fun getParentObject(): IFileList {
        return parent
    }

    override fun getMimeType(): String {
        if (path.endsWith(".kt")) return "text"
        if (path.toAbsolutePath().toFile().isDirectory) return "directory"
        return Files.probeContentType(path.toAbsolutePath())?.substringBefore('/') ?: "unknown"
    }

    override fun getContents(): File {
        return path.toFile()
    }
}