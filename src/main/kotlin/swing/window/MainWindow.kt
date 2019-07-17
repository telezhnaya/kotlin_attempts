package swing.window

import observer.FileSystem
import observer.Preview
import observer.filesystem.LocalFileSystem
import swing.*
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.Paths
import javax.swing.*


class MainWindow(private var fileSystem: FileSystem) : JFrame("Best file manager ever (or not)") {
    private val pathAndSettingsLayout = JPanel()
    private val path: JLabel
    private val pathConstraints = createGridBagConstraints(0, 0, 1.0, 0.0)

    private val fileListAndPreviewLayout = JPanel()
    private val fileListModel = DefaultListModel<String>()
    private val fileList = JList(fileListModel)
    private var preview: Component

    init {
        val screen = Toolkit.getDefaultToolkit().screenSize
        this.size = Dimension(screen.width / 3 * 2, screen.height / 3 * 2)
        this.defaultCloseOperation = DISPOSE_ON_CLOSE

        this.setLocationRelativeTo(null)
        val mainLayout = this.contentPane

        mainLayout.layout = GridBagLayout()
        pathAndSettingsLayout.layout = GridBagLayout()
        fileListAndPreviewLayout.layout = GridLayout(1, 2, 5, 0)


        path = JLabel(fileSystem.getFullPath())
        path.addMouseListener(PathChanger(this))
        pathAndSettingsLayout.add(path, pathConstraints)

        val settingsButton = JButton("FTP settings")
        settingsButton.addActionListener {
            val settingsWindow = SettingsWindow(settingsButton.text, this)
            settingsWindow.isVisible = true
        }
        pathAndSettingsLayout.add(settingsButton, createGridBagConstraints(1, 0, 0.0, 0.0))
        mainLayout.add(pathAndSettingsLayout, createGridBagConstraints(0, 0, 1.0, 0.0))


        fileListModel.refill(fileSystem.getFileList())
        fileList.init()
        fileListAndPreviewLayout.add(JScrollPane(fileList))

        preview = getComponent(fileSystem.getPreview(fileListModel[0]), fileList.size)
        fileListAndPreviewLayout.add(preview)
        mainLayout.add(fileListAndPreviewLayout, createGridBagConstraints(0, 1, 1.0, 1.0))
    }

    private fun JList<String>.init() {
        this.selectionMode = ListSelectionModel.SINGLE_SELECTION

        this.addListSelectionListener {
            if (this.selectedIndex != -1) showPreview()
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
            if (curPreview is Preview.Remote) {
                val downloadWindow = DownloadWindow(this, curPreview.inputStream, fileList.selectedValue)
                downloadWindow.isVisible = true
            }
            return
        }

        fileSystem = child
        fileListModel.refill(fileSystem.getFileList())
        fileList.selectedIndex = 0
        preview = getComponent(fileSystem.getPreview(fileListModel[0]), fileList.size)
        fileListAndPreviewLayout.reloadElement(preview, 1)
        path.reloadText(fileSystem.getFullPath())
    }

    private fun goBack() {
        val fileName = fileSystem.getCurrentFileName()
        val parent = fileSystem.goBack() ?: return

        fileSystem = parent
        fileListModel.refill(fileSystem.getFileList())
        preview = getComponent(fileSystem.getPreview(fileName), fileList.size)
        fileListAndPreviewLayout.reloadElement(preview, 1)
        path.reloadText(fileSystem.getFullPath())
    }

    private fun showPreview() {
        preview = getComponent(fileSystem.getPreview(fileList.selectedValue), fileList.size)
        fileListAndPreviewLayout.reloadElement(preview, 1)
    }

    private class PathChanger(private val parent: MainWindow) : MouseAdapter() {
        override fun mouseClicked(evt: MouseEvent) {
            if (evt.clickCount == 2 && parent.fileSystem is LocalFileSystem) { // hate this
                val editablePath = JTextField(parent.path.text)

                editablePath.addKeyListener(object : KeyAdapter() {
                    override fun keyReleased(ke: KeyEvent) {
                        if (ke.keyCode == KeyEvent.VK_ENTER) {
                            val path = File(editablePath.text)

                            if (!path.isAbsolute || !path.exists()) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "Invalid path, try again",
                                    "Error",
                                    JOptionPane.PLAIN_MESSAGE
                                )
                                parent.pathAndSettingsLayout.reloadPath(
                                    editablePath,
                                    parent.path,
                                    parent.pathConstraints
                                )
                                return
                            }

                            parent.path.text = editablePath.text
                            parent.pathAndSettingsLayout.reloadPath(editablePath, parent.path, parent.pathConstraints)

                            parent.fileSystem = LocalFileSystem(Paths.get(parent.path.text))
                            parent.fileListModel.refill(parent.fileSystem.getFileList())
                            parent.fileList.selectedIndex = 0
                            parent.preview = getComponent(
                                parent.fileSystem.getPreview(parent.fileListModel[0]),
                                parent.fileList.size
                            )
                            parent.fileListAndPreviewLayout.reloadElement(parent.preview, 1)
                        }
                    }
                })

                parent.pathAndSettingsLayout.reloadPath(parent.path, editablePath, parent.pathConstraints)
            }
        }

        private fun JPanel.reloadPath(oldPath: Component, newPath: Component, c: GridBagConstraints) {
            this.remove(oldPath)
            this.add(newPath, c)
            this.revalidate()
            this.repaint()
        }
    }
}