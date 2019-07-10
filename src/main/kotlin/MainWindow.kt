import java.awt.Component
import java.awt.Container
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.ListSelectionModel

class MainWindow(private val fileList: IFileList) : JFrame("Best file manager ever (or not)") {
    private val mainForm: Container
    private var left: JList<String>
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

        left = initLeft()
        right = initRight(fileList.getFirst())
        createFolderForm()

        // TODO add slider here and in the preview
        mainForm.add(left)
        mainForm.add(right)

    }

    internal fun initLeft(): JList<String> {
        return Drawer().getDrawable(fileList.getPreview(fileList.getCurrentDir())) as JList<String>
    }

    internal fun initRight(path: String): Component {
        return Drawer().getDrawable(fileList.getPreview(path))
    }

    internal fun createFolderForm() {
        // TODO fix selection (i want both mouse and arrows/enter)

        //left.selectedIndex = 0

        left.selectionMode = ListSelectionModel.SINGLE_SELECTION
        //left.selectionModel.setSelectionInterval(0, 0)

        // ide created me this lambda
        left.addListSelectionListener {
            right = Drawer().getDrawable(fileList.getPreview(left.selectedValue))
            if (mainForm.componentCount > 1) {
                mainForm.remove(1)
            }
            mainForm.add(right)
            mainForm.revalidate()
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
                if (ke.keyCode == KeyEvent.VK_RIGHT) {
                    openDirectory()
                } else if (ke.keyCode == KeyEvent.VK_LEFT) {
                    val cur = fileList.getCurrentDir()
                    fileList.goBack()
                    left = initLeft()
                    right = initRight(cur)
                    refresh()
                }
            }
        })
    }

    internal fun openDirectory() {
        if (!fileList.goForward(left.selectedValue)) return

        left = initLeft()
        right = initRight(fileList.getFirst())
        refresh()
    }

    internal fun refresh() {
        mainForm.removeAll()
        createFolderForm()
        mainForm.add(left)
        mainForm.add(right)
        mainForm.revalidate()
    }

}

