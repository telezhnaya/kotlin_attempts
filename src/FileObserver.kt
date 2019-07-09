import java.io.File


class FileObserver {

    fun getContents(file: File): Array<out File>? {
        if (file.isDirectory) {
            return file.listFiles()
        }
        return null
    }
}