package swing.window

import CANCEL
import SUBMIT
import observer.filesystem.LocalFileSystem
import observer.filesystem.ZipFileSystem
import swing.createGridBagConstraints
import swing.initErrorField
import swing.reloadText
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Point
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Paths
import javax.swing.*


class DownloadWindow(parent: JFrame, inputStream: InputStream, fileName: String) : JFrame("Download the file") {
    private val error = JTextArea()

    init {
        this.size = Dimension(350, 150)
        this.setLocationRelativeTo(null)

        val mainContainer = this.contentPane
        mainContainer.layout = GridBagLayout()

        mainContainer.add(
            JLabel("Please fill the path to download the file"),
            createGridBagConstraints(0, 0, 1.0, 0.0, 2)
        )

        val destination = JTextField()
        mainContainer.add(destination, createGridBagConstraints(0, 1, 1.0, 0.0, 2))
        error.initErrorField()
        mainContainer.add(error, createGridBagConstraints(0, 2, 1.0, 0.0, 2))

        val buttons = JPanel()
        buttons.layout = GridLayout(1, 2)

        val cancel = JButton(CANCEL)
        cancel.addActionListener {
            this.dispose()
        }
        buttons.add(cancel)

        val submit = JButton(SUBMIT)
        submit.addActionListener {
            if (!File(destination.text).isAbsolute) {
                error.reloadText("Please enter absolute path")
                return@addActionListener
            }

            try {
                inputStream.saveTo(File(destination.text), fileName)
                val app = MainWindow(
                    ZipFileSystem(
                        File(destination.text).resolve(fileName),
                        LocalFileSystem(Paths.get(destination.text))
                    )
                )
                val prevLocation = parent.location
                app.location = Point(prevLocation.x + 50, prevLocation.y + 50)
                this.dispose()
                app.isVisible = true
            } catch (e: FileAlreadyExistsException) {
                error.reloadText("The file already exists, please delete it at first")
            } catch (e: FileNotFoundException) {
                error.reloadText("The path does not exist, please try again")
            } catch (e: Exception) {
                error.reloadText(e.localizedMessage)
            }
        }
        buttons.add(submit)

        this.getRootPane().defaultButton = submit
        mainContainer.add(buttons, createGridBagConstraints(0, 3, 1.0, 0.0, 2))
    }

    private fun InputStream.saveTo(destination: File, fileName: String) {
        if (!destination.exists()) throw FileNotFoundException(destination.path)

        val fileToCreate = destination.resolve(fileName)
        if (fileToCreate.exists()) throw FileAlreadyExistsException(fileToCreate)

        this.use { it.copyTo(fileToCreate.outputStream()) }
    }
}