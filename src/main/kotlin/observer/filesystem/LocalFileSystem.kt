package observer.filesystem

import observer.FileSystem
import observer.Preview
import observer.preview.LocalPreview
import java.nio.file.Path

class LocalFileSystem(path: Path) : FileSystem {
    private var fullPath = path.toAbsolutePath()

    override fun goBack(): FileSystem? {
        // File.listRoots()
        val isNewPath = fullPath != fullPath.parent
        fullPath = fullPath.parent ?: fullPath
        return if (isNewPath) this else null
    }

    override fun goForward(file: String): FileSystem? {
        if (file == "..") return goBack()

        val newPath = fullPath.resolve(file)
        if (file.endsWith(".zip"))
            return ZipFileSystem(newPath, this)

        val isNewPath = newPath.toFile().isDirectory
        if (isNewPath) fullPath = newPath
        return if (isNewPath) this else null
    }

    override fun getPreview(file: String): Preview {
        return LocalPreview(fullPath.resolve(file))
    }

    override fun getFullPath(): String {
        return fullPath.toString()
    }

    override fun getCurrentFileName(): String {
        return fullPath.toFile().name
    }
}