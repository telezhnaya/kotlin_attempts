import observer.FTPFileList
import org.apache.commons.net.ftp.FTPClient
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.net.ConnectException
import javax.swing.*


class SettingsWindow(header: String, parent: JFrame) : JFrame(header) {
    private val error = JLabel(" ")

    init {
        this.size = Dimension(350, 150)
        this.setLocationRelativeTo(null)

        val mainContainer = this.contentPane
        mainContainer.layout = GridBagLayout()

        val settings = JPanel()
        settings.layout = GridBagLayout()

        val address = JTextField()
        settings.add(JLabel("Server address"), createGridBagConstraints(0, 0, 0.0, 0.0))
        settings.add(address, createGridBagConstraints(1, 0, 1.0, 0.0))

        val user = JTextField()
        settings.add(JLabel("User"), createGridBagConstraints(0, 1, 0.0, 0.0))
        settings.add(user, createGridBagConstraints(1, 1, 1.0, 0.0))

        val password = JPasswordField()
        settings.add(JLabel("Password"), createGridBagConstraints(0, 2, 0.0, 0.0))
        settings.add(password, createGridBagConstraints(1, 2, 1.0, 0.0))

        mainContainer.add(settings, createGridBagConstraints(0, 0, 1.0, 1.0))
        mainContainer.add(error, createGridBagConstraints(0, 1, 1.0, 0.0, 2))

        val buttons = JPanel()
        buttons.layout = GridLayout(1, 2)

        val cancel = JButton("Cancel")
        cancel.addActionListener {
            this.dispose()
        }
        buttons.add(cancel)
        val submit = JButton("Submit")
        submit.addActionListener {
            val client = FTPClient()

            try {
                client.connect(address.text)
            } catch (e: ConnectException) {
                showError()
                return@addActionListener
            }
            if (client.login(user.text, password.text)) {
                val fileList = FTPFileList(client)
                val app = MainWindow(fileList)
                app.isVisible = true
                parent.dispose()
                this.dispose()
            } else {
                showError()
            }
        }
        buttons.add(submit)

        mainContainer.add(buttons, createGridBagConstraints(0, 2, 1.0, 0.0, 2))

    }

    private fun showError() {
        error.text = "Invalid credentials. Please try again"
        error.foreground = Color.RED
        error.revalidate()
    }
}