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
        // window will be centered and take 2/3 of screen
        this.setBounds(screen.width / 6, screen.height / 6, screen.width / 3 * 2, screen.height / 3 * 2)
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val mainLayout = this.contentPane
        val settingsLayout = JPanel()

        mainLayout.layout = GridBagLayout()
        settingsLayout.layout = GridBagLayout()
        pathsAndPreviewLayout.layout = GridLayout(1, 2, 5, 0)

        currentPath = JLabel(fileList.getFullPath())
        settingsLayout.add(currentPath, createGridBagConstraints(0, 0, 1.0, 0.0))

        val settingsButton = JButton("Settings")
        settingsLayout.add(settingsButton, createGridBagConstraints(1, 0, 0.0, 0.0))
        mainLayout.add(settingsLayout, createGridBagConstraints(0, 0, 1.0, 0.0))


        fillPathsForm(fileList.getPreview("").getFileList())
        paths = JList(pathsModel)
        initPathsComponent()
        pathsAndPreviewLayout.add(JScrollPane(paths))

        preview = fileList.getPreview(pathsModel[0]).getDrawable(paths.size)
        pathsAndPreviewLayout.add(preview)
        mainLayout.add(pathsAndPreviewLayout, createGridBagConstraints(0, 1, 1.0, 1.0))
    }

    private fun createGridBagConstraints(gridx: Int, gridy: Int, weightx: Double, weighty: Double): GridBagConstraints {
        return GridBagConstraints(
            gridx,
            gridy,
            1,
            1,
            weightx,
            weighty,
            10,
            GridBagConstraints.BOTH,
            Insets(0, 0, 0, 0),
            0,
            0
        )
    }

    private fun fillPathsForm(files: List<String>) {
        pathsModel.removeAllElements()
        pathsModel.add(0, "..")
        pathsModel.addAll(files)
    }

    private fun initPathsComponent() {
        paths.selectionMode = ListSelectionModel.SINGLE_SELECTION

        paths.addListSelectionListener {
            if (paths.selectedIndex != -1) {
                preview = fileList.getPreview(paths.selectedValue).getDrawable(preview.size)
                reloadPreviewForm()
            }
        }

        paths.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                if (evt.clickCount == 2) {
                    openDirectoryPath()
                }
            }
        })

        paths.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(ke: KeyEvent) {
                when (ke.keyCode) {
                    KeyEvent.VK_RIGHT -> openDirectoryPath()
                    KeyEvent.VK_ENTER -> openDirectoryPath()
                    KeyEvent.VK_LEFT -> {
                        val cur = fileList.getCurrentFileName()
                        fileList = fileList.goBack()
                        fillPathsForm(fileList.getPreview("").getFileList())
                        preview = fileList.getPreview(cur).getDrawable(preview.size)
                        reloadPreviewForm()
                        currentPath.text = fileList.getFullPath()
                        currentPath.revalidate()
                    }
                }
            }
        })
    }

    private fun openDirectoryPath() {
        if (paths.selectedValue == null) return

        fileList = fileList.goForward(paths.selectedValue)
        fillPathsForm(fileList.getPreview("").getFileList())
        paths.selectedIndex = 0
        preview = fileList.getPreview(pathsModel[0]).getDrawable(preview.size)
        reloadPreviewForm()
        currentPath.text = fileList.getFullPath()
        currentPath.revalidate()
    }

    private fun reloadPreviewForm() {
        while (pathsAndPreviewLayout.componentCount > 1) {
            pathsAndPreviewLayout.remove(1)
        }
        pathsAndPreviewLayout.add(preview, 1)
        pathsAndPreviewLayout.revalidate()
    }
}