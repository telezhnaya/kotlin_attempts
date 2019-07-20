package observer

import java.io.InputStream

sealed class PreviewData {
    data class Directory(val paths: List<String>) : PreviewData()
    data class Image(val inputStream: InputStream) : PreviewData()
    data class Text(val inputStream: InputStream) : PreviewData()
    data class Remote(val inputStream: InputStream) : PreviewData()
    object Unhandled : PreviewData()
}