package swing

import observer.IFileList
import observer.LocalFileList
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Point
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths
import javax.swing.*


class DownloadWindow(parent: JFrame, fileList: IFileList, path: String) : JFrame("Download the file") {
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
                showError(error, "Please enter absolute path")
                return@addActionListener
            }
            try {
                fileList.downloadFile(path, destination.text)
                val app = MainWindow(LocalFileList(Paths.get(destination.text)))
                val prevLocation = parent.location
                app.location = Point(prevLocation.x + 50, prevLocation.y + 50)
                this.dispose()
                app.isVisible = true
            } catch (e: FileAlreadyExistsException) {
                showError(error, "The file already exists, please delete it at first")
            } catch (e: FileNotFoundException) {
                showError(error, "The path does not exist, please try again")
            } catch (e: Exception) {
                showError(error, "Sorry, something goes wrong")
            }
        }
        buttons.add(submit)

        mainContainer.add(buttons, createGridBagConstraints(0, 3, 1.0, 0.0, 2))
    }
}