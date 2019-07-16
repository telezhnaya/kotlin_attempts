package observer

import java.io.InputStream

sealed class Preview {
    data class Directory(val paths: List<String>) : Preview()
    data class Image(val inputStream: InputStream) : Preview()
    data class Text(val inputStream: InputStream) : Preview()
    data class Remote(val inputStream: InputStream) : Preview()
    object Unhandled : Preview()
}