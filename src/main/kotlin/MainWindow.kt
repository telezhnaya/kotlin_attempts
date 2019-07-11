import observer.IFileList
import java.awt.Component
import java.awt.Container
import java.awt.GridLayout
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*


class MainWindow(private val fileList: IFileList) : JFrame("Best file manager ever (or not)") {
    private val mainForm: Container

    private val left: JList<String>
    private val leftData = DefaultListModel<String>()

    private var right: Component

    init {
        val screen = Toolkit.getDefaultToolkit().screenSize
        // window will be centered and take 2/3 of screen
        this.setBounds(screen.width / 6, screen.height / 6, screen.width / 3 * 2, screen.height / 3 * 2)
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        mainForm = this.contentPane
        mainForm.layout = GridLayout(1, 2)

//        // TODO ftp module, set local path manually
//        val settings = JButton("Settings")
//        settings.preferredSize = Dimension(10, 50)
//        mainForm.add(settings)

        fillLeftForm(fileList.getPreview("").getFileList())
        left = JList(leftData)
        initLeftComponent()
        mainForm.add(JScrollPane(left))

        right = fileList.getPreview(leftData[0]).getDrawable(left.size) // TODO get rid of left here
        mainForm.add(right)
    }

    private fun fillLeftForm(files: List<String>) {
        leftData.removeAllElements()
        leftData.add(0, "..")
        leftData.addAll(files)
    }

    private fun initLeftComponent() {
        left.selectionMode = ListSelectionModel.SINGLE_SELECTION

        left.addListSelectionListener {
            if (left.selectedIndex != -1) {
                right = fileList.getPreview(left.selectedValue).getDrawable(right.size)
                reloadRightForm()
            }
        }

        left.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                if (evt.clickCount == 2) {
                    openDirectory()
                }
            }
        })

        left.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(ke: KeyEvent) {
                when (ke.keyCode) {
                    KeyEvent.VK_RIGHT -> openDirectory()
                    KeyEvent.VK_ENTER -> openDirectory()
                    KeyEvent.VK_LEFT -> {
                        val cur = fileList.getCurrentDir()
                        fileList.goBack()
                        fillLeftForm(fileList.getPreview("").getFileList())
                        right = fileList.getPreview(cur).getDrawable(right.size)
                        reloadRightForm()
                    }
                }
            }
        })
    }

    private fun openDirectory() {
        if (left.selectedValue == null || !fileList.goForward(left.selectedValue)) return

        fillLeftForm(fileList.getPreview("").getFileList())
        left.selectedIndex = 0
        right = fileList.getPreview(leftData[0]).getDrawable(right.size)
        reloadRightForm()
    }

    private fun reloadRightForm() {
        while (mainForm.componentCount > 1) {
            mainForm.remove(1)
        }
        mainForm.add(right)
        mainForm.revalidate()
    }
}