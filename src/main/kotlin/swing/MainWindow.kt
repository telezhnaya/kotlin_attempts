package swing

import observer.IFileList
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*


class MainWindow(private var fileList: IFileList) : JFrame("Best file manager ever (or not)") {
    private val currentPath: JLabel

    private val pathsAndPreviewLayout = JPanel()
    private val paths: JList<String>
    private val pathsModel = DefaultListModel<String>()
    private var preview: Component

    init {
        val screen = Toolkit.getDefaultToolkit().screenSize
        this.size = Dimension(screen.width / 3 * 2, screen.height / 3 * 2)
        this.setLocationRelativeTo(null)
        val mainLayout = this.contentPane
        val settingsLayout = JPanel()

        mainLayout.layout = GridBagLayout()
        settingsLayout.layout = GridBagLayout()
        pathsAndPreviewLayout.layout = GridLayout(1, 2, 5, 0)

        currentPath = JLabel(fileList.getFullPath())
        settingsLayout.add(currentPath, createGridBagConstraints(0, 0, 1.0, 0.0))

        val settingsButton = JButton("FTP settings")
        settingsButton.addActionListener {
            val settingsWindow = SettingsWindow(settingsButton.text, this)
            settingsWindow.isVisible = true
        }
        settingsLayout.add(settingsButton, createGridBagConstraints(1, 0, 0.0, 0.0))
        mainLayout.add(settingsLayout, createGridBagConstraints(0, 0, 1.0, 0.0))


        fillPathsForm(fileList.getPreview().getFileList())
        paths = JList(pathsModel)
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
            if (paths.selectedIndex != -1) reloadPreview()
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
        preview = fileList.getPreview(pathsModel[0]).getDrawable(preview.size)
        reloadPreviewForm()
        currentPath.text = fileList.getFullPath()
        currentPath.revalidate()
    }

    private fun goBack() {
        val cur = fileList.getCurrentFileName()
        val (isChanged, res) = fileList.goBack()
        if (!isChanged) return

        fileList = res
        fillPathsForm(fileList.getPreview().getFileList())
        preview = fileList.getPreview(cur).getDrawable(preview.size)
        reloadPreviewForm()
        currentPath.text = fileList.getFullPath()
        currentPath.revalidate()
    }

    private fun reloadPreview() {
        preview = if (fileList.willDownloadHelp(paths.selectedValue))
            fileList.getPreview(paths.selectedValue).getDrawable(
                preview.size,
                "Click Enter to extract this file"
            )
        else
            fileList.getPreview(paths.selectedValue).getDrawable(preview.size)

        reloadPreviewForm()
    }

    private fun reloadPreviewForm() {
        while (pathsAndPreviewLayout.componentCount > 1) {
            pathsAndPreviewLayout.remove(1)
        }
        pathsAndPreviewLayout.add(preview, 1)
        pathsAndPreviewLayout.revalidate()
    }
}