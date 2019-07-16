package swing.window

import observer.filesystem.LocalFileSystem
import swing.createGridBagConstraints
import swing.reloadText
import java.awt.*
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Paths
import javax.swing.*


class DownloadWindow(parent: JFrame, inputStream: InputStream, fileName: String) : JFrame("Download the file") {
    private val error = JLabel(" ")

    init {
        this.size = Dimension(350, 120)
        this.setLocationRelativeTo(null)

        val mainContainer = this.contentPane
        mainContainer.layout = GridBagLayout()

        mainContainer.add(
            JLabel("Please fill the path to download the file"),
            createGridBagConstraints(0, 0, 1.0, 0.0, 2)
        )

        val destination = JTextField()
        mainContainer.add(destination, createGridBagConstraints(0, 1, 1.0, 0.0, 2))
        error.foreground = Color.RED
        mainContainer.add(error, createGridBagConstraints(0, 2, 1.0, 0.0, 2))

        val buttons = JPanel()
        buttons.layout = GridLayout(1, 2)

        val cancel = JButton("Cancel")
        cancel.addActionListener {
            this.dispose()
        }
        buttons.add(cancel)

        val submit = JButton("Submit")
        submit.addActionListener {
            if (!File(destination.text).isAbsolute) {
                error.reloadText("Please enter absolute path")
                return@addActionListener
            }

            try {
                inputStream.saveTo(File(destination.text), fileName)
                val app = MainWindow(LocalFileSystem(Paths.get(destination.text)))
                val prevLocation = parent.location
                app.location = Point(prevLocation.x + 50, prevLocation.y + 50)
                this.dispose()
                app.isVisible = true
            } catch (e: FileAlreadyExistsException) {
                error.reloadText("The file already exists, please delete it at first")
            } catch (e: FileNotFoundException) {
                error.reloadText("The path does not exist, please try again")
            } catch (e: Exception) {
                error.reloadText("Sorry, something goes wrong")
            }
        }
        buttons.add(submit)

        mainContainer.add(buttons, createGridBagConstraints(0, 3, 1.0, 0.0, 2))
    }

    private fun InputStream.saveTo(destination: File, fileName: String) {
        if (!destination.exists()) throw FileNotFoundException(destination.path)

        val fileToCreate = destination.resolve(fileName)
        if (fileToCreate.exists()) throw FileAlreadyExistsException(fileToCreate)

        this.copyTo(fileToCreate.outputStream())
        // client.completePendingCommand() ?
    }
}