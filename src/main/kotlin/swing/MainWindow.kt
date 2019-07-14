package swing

import observer.IFileList
import observer.LocalFileList
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.Paths
import javax.swing.*


class MainWindow(private var fileList: IFileList) : JFrame("Best file manager ever (or not)") {
    private val currentPath: JLabel

    private val pathsAndPreviewLayout = JPanel()
    private val settingsLayout = JPanel()
    private val settingsPathConstraints = createGridBagConstraints(0, 0, 1.0, 0.0)
    private val pathsModel = DefaultListModel<String>()
    private val paths = JList(pathsModel)
    private var preview: Component

    init {
        val screen = Toolkit.getDefaultToolkit().screenSize
        this.size = Dimension(screen.width / 3 * 2, screen.height / 3 * 2)
        this.setLocationRelativeTo(null)
        val mainLayout = this.contentPane

        mainLayout.layout = GridBagLayout()
        settingsLayout.layout = GridBagLayout()
        pathsAndPreviewLayout.layout = GridLayout(1, 2, 5, 0)

        currentPath = JLabel(fileList.getFullPath())

        currentPath.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                if (evt.clickCount == 2 && fileList is LocalFileList) { // hate this
                    val newPath = JTextField(currentPath.text)
                    newPath.addKeyListener(object : KeyAdapter() {
                        override fun keyReleased(ke: KeyEvent) {
                            if (ke.keyCode == KeyEvent.VK_ENTER) {
                                val path = File(newPath.text)
                                if (!path.isAbsolute || !path.exists()) {
                                    JOptionPane.showMessageDialog(
                                        null,
                                        "Invalid path, try again",
                                        "Error",
                                        JOptionPane.PLAIN_MESSAGE
                                    )
                                    reloadSettingsForm(newPath, currentPath)
                                    return
                                }
                                currentPath.text = newPath.text
                                reloadSettingsForm(newPath, currentPath)

                                fileList = LocalFileList(Paths.get(currentPath.text))
                                fillPathsForm(fileList.getPreview().getFileList())
                                paths.selectedIndex = 0
                                preview = fileList.getPreview(pathsModel[0]).getDrawable(paths.size)
                                reloadPreviewForm()
                            }
                        }
                    })
                    reloadSettingsForm(currentPath, newPath)
                }
            }
        })

        settingsLayout.add(currentPath, settingsPathConstraints)

        val settingsButton = JButton("FTP settings")
        settingsButton.addActionListener {
            val settingsWindow = SettingsWindow(settingsButton.text, this)
            settingsWindow.isVisible = true
        }
        settingsLayout.add(settingsButton, createGridBagConstraints(1, 0, 0.0, 0.0))
        mainLayout.add(settingsLayout, createGridBagConstraints(0, 0, 1.0, 0.0))


        fillPathsForm(fileList.getPreview().getFileList())
        initPathsComponent()
        pathsAndPreviewLayout.add(JScrollPane(paths))

        preview = fileList.getPreview(pathsModel[0]).getDrawable(paths.size)
        pathsAndPreviewLayout.add(preview)
        mainLayout.add(pathsAndPreviewLayout, createGridBagConstraints(0, 1, 1.0, 1.0))
    }

    private fun fillPathsForm(files: List<String>) {
        pathsModel.removeAllElements()
        pathsModel.add(0, "..")
        pathsModel.addAll(files)
    }

    private fun initPathsComponent() {
        paths.selectionMode = ListSelectionModel.SINGLE_SELECTION

        paths.addListSelectionListener {
            if (paths.selectedIndex != -1) showPreview()
        }

        paths.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                if (evt.clickCount == 2) goForward()
            }
        })

        paths.addKeyListener(object : KeyAdapter() {
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
        if (paths.selectedValue == null) return

        // ugh, I have feature request to Kotlin: support val/var keyword into parenthesis
        // (not only create new val/var, but assigning into existing var)
        val (isChanged, res) = fileList.goForward(paths.selectedValue)

        if (!isChanged) {
            if (fileList.willDownloadHelp(paths.selectedValue)) {
                val downloadWindow = DownloadWindow(this, fileList, paths.selectedValue)
                downloadWindow.isVisible = true
            }
            return
        }

        fileList = res
        fillPathsForm(fileList.getPreview().getFileList())
        paths.selectedIndex = 0
        preview = fileList.getPreview(pathsModel[0]).getDrawable(paths.size)
        reloadPreviewForm()
        currentPath.text = fileList.getFullPath()
        currentPath.revalidate()
        currentPath.repaint()
    }

    private fun goBack() {
        val cur = fileList.getCurrentFileName()
        val (isChanged, res) = fileList.goBack()
        if (!isChanged) return

        fileList = res
        fillPathsForm(fileList.getPreview().getFileList())
        preview = fileList.getPreview(cur).getDrawable(paths.size)
        reloadPreviewForm()
        currentPath.text = fileList.getFullPath()
        currentPath.revalidate()
        currentPath.repaint()
    }

    private fun showPreview() {
        preview = if (fileList.willDownloadHelp(paths.selectedValue))
            fileList.getPreview(paths.selectedValue).getDrawable(
                preview.size,
                "Click Enter to extract this file"
            )
        else
            fileList.getPreview(paths.selectedValue).getDrawable(paths.size)

        reloadPreviewForm()
    }

    private fun reloadPreviewForm() {
        while (pathsAndPreviewLayout.componentCount > 1) {
            pathsAndPreviewLayout.remove(1)
        }
        pathsAndPreviewLayout.add(preview, 1)
        pathsAndPreviewLayout.revalidate()
        pathsAndPreviewLayout.repaint()
    }

    private fun reloadSettingsForm(oldPath: Component, newPath: Component) {
        settingsLayout.remove(oldPath)
        settingsLayout.add(newPath, settingsPathConstraints)
        settingsLayout.revalidate()
        settingsLayout.repaint()
    }
}