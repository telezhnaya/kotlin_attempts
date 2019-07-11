import java.awt.Component
import java.awt.Container
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.ListSelectionModel

class MainWindow(private val fileList: IFileList) : JFrame("Best file manager ever (or not)") {
    private val mainForm: Container

    private val left: JList<String>
    private val leftData = DefaultListModel<String>()

    private var right: Component

    init {
        // TODO make size depends on screen size
        this.setBounds(100, 100, 1000, 500)
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        mainForm = this.contentPane
        mainForm.layout = GridLayout(1, 2)

//        // TODO ftp module, set local path manually
//        val settings = JButton("Settings")
//        settings.preferredSize = Dimension(10, 50)
//        mainForm.add(settings)

        fillLeftForm(fileList.getPreview(fileList.getCurrentDir()).getFileList())
        left = JList(leftData)
        //val listScroller = JScrollPane(left);
        //listScroller.setPreferredSize(new Dimension(250, 80));
        initLeftComponent()
        mainForm.add(left)

        right = fileList.getPreview(leftData[0]).getDrawable()
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
                right = fileList.getPreview(left.selectedValue).getDrawable()
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
                        fillLeftForm(fileList.getPreview(fileList.getCurrentDir()).getFileList())
                        right = fileList.getPreview(cur).getDrawable()
                        reloadRightForm()
                    }
                }
            }
        })
    }

    private fun openDirectory() {
        if (!fileList.goForward(left.selectedValue)) return

        fillLeftForm(fileList.getPreview(fileList.getCurrentDir()).getFileList())
        left.selectedIndex = 0
        right = fileList.getPreview(leftData[0]).getDrawable()
        reloadRightForm()
    }

    private fun reloadRightForm() {
        while (mainForm.componentCount > 1) {
            mainForm.remove(1)
        }
        mainForm.add(right)
        right.revalidate()
    }
}