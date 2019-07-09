import java.awt.Container
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class MainWindow constructor(startPath: File) : JFrame("Best file manager ever (or not)") {
    private var curFolder: JList<File>

    private val mainForm: Container
    private val elementsList = 0
    private val elementPreview = 1
    private val elementsCount = 2

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

        curFolder = createFolderForm(startPath)
        // TODO add slider here and in the preview
        mainForm.add(curFolder, elementsList)
    }

    internal fun createFolderForm(file: File): JList<File> {
        val res = FileObserver().getContents(file) as JList<File>

        // TODO fix selection (i want both mouse and arrows/enter)

        //res.selectedIndex = 0
        res.selectionMode = ListSelectionModel.SINGLE_SELECTION

        res.addListSelectionListener(ItemPreviewer())
        res.addMouseListener(DoubleClick())
        res.addKeyListener(EnterPress())

        // NPE...
        //res.selectedIndex = 0
        // it would be perfect to invoke valueChanged here


        return res
    }

    internal fun openDirectory() {
        if (! curFolder.selectedValue.isDirectory) return

        // TODO is it posibble not to recreate this list?
        curFolder = createFolderForm(curFolder.selectedValue)

        while (mainForm.componentCount > elementsList)
            mainForm.remove(mainForm.componentCount - 1)

        mainForm.add(curFolder)
        mainForm.revalidate()
    }

    internal inner class ItemPreviewer : ListSelectionListener {
        override fun valueChanged(p0: ListSelectionEvent?) {
            val contents = FileObserver().getContents(curFolder.selectedValue, need_parent = false)
            if (mainForm.componentCount == elementsCount) {
                mainForm.remove(elementPreview)
            }
            mainForm.add(contents, elementPreview)
            mainForm.revalidate()
        }
    }

    internal inner class DoubleClick : MouseAdapter() {
        override fun mouseClicked(evt: MouseEvent) {
            if (evt.clickCount == 2) {
                openDirectory()
            }
        }
    }

    internal inner class EnterPress : KeyAdapter() {
        override fun keyReleased(ke: KeyEvent) {
            if (ke.keyCode == KeyEvent.VK_ENTER) {
                openDirectory()
            }
        }
    }
}

