import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class MainWindow constructor(startPath: File) : JFrame("Best file manager ever (or not)") {

    private val curFolder = JList(FileObserver().getContents(startPath))
    private val mainForm: Container
    private val elementsCount = 4
    private val elementPreview = 3

    init {
        // TODO make size depends on screen size
        this.setBounds(100, 100, 1000, 500)
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        mainForm = this.contentPane
        mainForm.layout = GridLayout(2, 2)

        // TODO find more beautiful element
        // it's better to put "back" into the list, so arrows and Enter will work also
        val returnBack = JButton("Back")
        returnBack.preferredSize = Dimension(10, 50) // does not work
        mainForm.add(returnBack)

        // TODO ftp module, set local path manually
        val settings = JButton("Settings")
        settings.preferredSize = Dimension(10, 50)
        mainForm.add(settings)

        curFolder.selectionMode = ListSelectionModel.SINGLE_SELECTION
        curFolder.addListSelectionListener(ItemPreviewer())
        curFolder.addMouseListener(DirectoryChanger())
        // TODO add slider here and in the preview
        mainForm.add(curFolder)
    }

    internal inner class ItemPreviewer : ListSelectionListener {
        override fun valueChanged(p0: ListSelectionEvent?) {
            //curFolder.selectedIndex

            val label = JLabel(curFolder.selectedValue.toString())
            if (mainForm.componentCount == elementsCount) {
                mainForm.remove(elementPreview)
            }
            // TODO working preview for just anything
            mainForm.add(label)
            mainForm.revalidate()
        }
    }

    internal inner class DirectoryChanger : MouseAdapter() {
        override fun mouseClicked(evt: MouseEvent) {
            if (evt.clickCount == 2) {
                //val index = curFolder.locationToIndex(evt.point)
                val prev = mainForm.getComponent(elementPreview)
                if (prev is JLabel) {
                    prev.text = "Yaay you pressed on " + curFolder.selectedValue.toString()
                    mainForm.revalidate()
                }
            }
        }
    }
}
