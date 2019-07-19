package swing.window

import observer.FileSystem
import observer.PreviewData
import observer.filesystem.LocalFileSystem
import org.apache.commons.net.ftp.FTPClient
import swing.*
import java.awt.*
import java.awt.event.*
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import javax.swing.*


class MainWindow(private var fileSystem: FileSystem, ftpClient: FTPClient? = null) :
    JFrame("Best file manager ever (or not)") {

    private val pathAndSettingsLayout = JPanel()
    private val path = JLabel()
    private val pathConstraints = createGridBagConstraints(0, 0, 1.0, 0.0)

    private val fileListAndPreviewLayout = JPanel()
    private val fileListModel = DefaultListModel<String>()
    private val fileList = JList(fileListModel)
    private var preview: Component


    private var previewWorker = getPreviewWorker()
    private var pathWorker = getPathWorker()
    private var fileListWorker = getFileListWorker()

    init {
        val screen = Toolkit.getDefaultToolkit().screenSize
        this.size = Dimension(screen.width / 3 * 2, screen.height / 3 * 2)
        this.defaultCloseOperation = DISPOSE_ON_CLOSE

        this.setLocationRelativeTo(null)
        val mainLayout = this.contentPane

        mainLayout.layout = GridBagLayout()
        pathAndSettingsLayout.layout = GridBagLayout()
        fileListAndPreviewLayout.layout = GridLayout(1, 2, 5, 0)

        showPath()
        path.name = PATH_LABEL
        path.addMouseListener(PathStartChangeListener(this))
        pathAndSettingsLayout.add(path, pathConstraints)

        val ftpSettingsButton = JButton("FTP settings")
        ftpSettingsButton.name = FTP_SETTINGS_BUTTON
        ftpSettingsButton.addActionListener {
            val ftpSettingsWindow = FTPSettingsWindow(ftpSettingsButton.text, this)
            ftpSettingsWindow.isVisible = true
        }
        pathAndSettingsLayout.add(ftpSettingsButton, createGridBagConstraints(1, 0, 0.0, 0.0))

        if (ftpClient != null) {
            val closeFtpButton = JButton("Back to local")
            closeFtpButton.addActionListener(FTPCloseListener(this, ftpClient))
            pathAndSettingsLayout.add(closeFtpButton, createGridBagConstraints(2, 0, 0.0, 0.0))
        }

        mainLayout.add(pathAndSettingsLayout, createGridBagConstraints(0, 0, 1.0, 0.0))

        fileList.init()
        fileList.name = FILE_JLIST
        showFileList()
        fileListAndPreviewLayout.add(JScrollPane(fileList))

        preview = getComponent(fileSystem.getPreview(fileListModel[0]), fileList.size)
        fileListAndPreviewLayout.add(preview)
        mainLayout.add(fileListAndPreviewLayout, createGridBagConstraints(0, 1, 1.0, 1.0))
    }

    private fun JList<String>.init() {
        this.selectionMode = ListSelectionModel.SINGLE_SELECTION

        this.addListSelectionListener {
            showPreview()
        }

        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                if (evt.clickCount == 2) goForward()
            }
        })

        this.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(ke: KeyEvent) {
                when (ke.keyCode) {
                    KeyEvent.VK_RIGHT -> goForward()
                    KeyEvent.VK_ENTER -> goForward()
                    KeyEvent.VK_LEFT -> goBack()
                }
            }
        })
    }

    private fun goForward() {
        if (fileList.selectedValue == null) return
        val child = fileSystem.goForward(fileList.selectedValue)

        if (child == null) {
            val curPreview = fileSystem.getPreview(fileList.selectedValue)
            if (curPreview is PreviewData.Remote) {
                val downloadWindow = DownloadWindow(this, curPreview.inputStream, fileList.selectedValue)
                downloadWindow.isVisible = true
            }
            return
        }

        fileSystem = child
        showFileList()
        showPreview()
        showPath()
    }

    private fun goBack() {
        val fileName = fileSystem.getCurrentFileName()
        val parent = fileSystem.goBack() ?: return

        fileSystem = parent
        showFileList()
        showPreview(fileName)
        showPath()
    }

    private fun showPreview(item: String? = null) {
        previewWorker.cancel(true)
        previewWorker = getPreviewWorker(item)
        previewWorker.execute()
    }

    private fun showPath() {
        path.reloadText("")

        pathWorker.cancel(true)
        pathWorker = getPathWorker()
        pathWorker.execute()
    }

    private fun showFileList() {
        fileListModel.removeAllElements()
        fileListModel.add(0, "..")

        fileListWorker.cancel(true)
        fileListWorker = getFileListWorker()
        fileListWorker.execute()
    }

    private class PathStartChangeListener(private val parent: MainWindow) : MouseAdapter() {
        override fun mouseClicked(evt: MouseEvent) {
            if (evt.clickCount == 2 && parent.fileSystem is LocalFileSystem) { // hate this
                val editablePath = JTextField(parent.path.text)
                editablePath.addKeyListener(PathFinishChangeListener(parent, editablePath))
                parent.reloadPath(parent.path, editablePath)
            }
        }
    }

    private class PathFinishChangeListener(
        private val parent: MainWindow,
        private val newPath: JTextField
    ) : KeyAdapter() {

        override fun keyReleased(ke: KeyEvent) {
            when (ke.keyCode) {
                KeyEvent.VK_ESCAPE -> parent.reloadPath(newPath, parent.path)
                KeyEvent.VK_ENTER -> {
                    val path = File(newPath.text)

                    if (!path.isAbsolute || !path.exists()) {
                        JOptionPane.showMessageDialog(null, "Invalid path", "Error", JOptionPane.PLAIN_MESSAGE)
                        parent.reloadPath(newPath, parent.path)
                        return
                    }

                    parent.path.text = newPath.text
                    parent.reloadPath(newPath, parent.path)

                            parent.fileSystem = LocalFileSystem(Paths.get(parent.path.text))
                            parent.showFileList()
                            parent.showPreview()
                        }
                    }
                }
    }

    private fun reloadPath(oldPath: Component, newPath: Component) {
        pathAndSettingsLayout.remove(oldPath)
        pathAndSettingsLayout.add(newPath, pathConstraints)
        pathAndSettingsLayout.revalidate()
        pathAndSettingsLayout.repaint()
    }

    private class FTPCloseListener(private val parent: JFrame, private val client: FTPClient) : ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {
            client.disconnect()
            parent.dispose()
            val app = MainWindow(LocalFileSystem(Paths.get("")))
            app.setLocationRelativeTo(null)
            app.isVisible = true
        }
    }


    private fun getPreviewWorker(item: String? = null): SwingWorker<Component, Nothing> {
        return object : SwingWorker<Component, Nothing>() {

            override fun doInBackground(): Component {
                fileListWorker.get() // can't load preview without loaded filelist
                if (item != null)
                    fileList.setSelectedValue(item, true)
                if (fileList.selectedIndex == -1)
                    fileList.selectedIndex = 0
                return getComponent(fileSystem.getPreview(fileList.selectedValue), fileList.size)
            }

            override fun done() {
                try {
                    preview = get()
                    fileListAndPreviewLayout.reloadElement(preview, 1)
                } catch (e: CancellationException) {
                } catch (e: ExecutionException) {
                }
            }
        }
    }

    private fun getPathWorker(): SwingWorker<String, Nothing> {
        return object : SwingWorker<String, Nothing>() {

            override fun doInBackground(): String {
                return fileSystem.getFullPath()
            }

            override fun done() {
                try {
                    path.reloadText(get())
                } catch (e: CancellationException) {
                }
            }
        }
    }

    private fun getFileListWorker(): SwingWorker<List<String>, Nothing> {
        return object : SwingWorker<List<String>, Nothing>() {

            override fun doInBackground(): List<String> {
                return fileSystem.getFileList()
            }

            override fun done() {
                try {
                    fileListModel.addAll(get())
                } catch (e: CancellationException) {
                }
            }
        }
    }
}