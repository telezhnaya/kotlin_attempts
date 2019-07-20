package swing.window

import CANCEL
import FTP_PREFIX
import SUBMIT
import observer.filesystem.FTPFileSystem
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import swing.createGridBagConstraints
import swing.initErrorField
import swing.reloadText
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.ConnectException
import javax.swing.*


class FTPSettingsWindow(header: String, parent: JFrame) : JFrame(header) {
    private val error = JTextArea()

    init {
        this.size = Dimension(350, 200)
        this.setLocationRelativeTo(null)

        val mainContainer = this.contentPane
        mainContainer.layout = GridBagLayout()

        val settings = JPanel()
        settings.layout = GridBagLayout()

        val address = JTextField(FTP_PREFIX)
        settings.add(JLabel("Server address"), createGridBagConstraints(0, 0, 0.0, 0.0))
        settings.add(address, createGridBagConstraints(1, 0, 1.0, 0.0))

        val user = JTextField()
        settings.add(JLabel("User"), createGridBagConstraints(0, 1, 0.0, 0.0))
        settings.add(user, createGridBagConstraints(1, 1, 1.0, 0.0))

        val password = JPasswordField()
        settings.add(JLabel("Password"), createGridBagConstraints(0, 2, 0.0, 0.0))
        settings.add(password, createGridBagConstraints(1, 2, 1.0, 0.0))

        mainContainer.add(settings, createGridBagConstraints(0, 0, 1.0, 1.0))
        error.initErrorField()
        mainContainer.add(error, createGridBagConstraints(0, 1, 1.0, 0.0, 2))

        val buttons = JPanel()
        buttons.layout = GridLayout(1, 2)

        val cancel = JButton(CANCEL)
        cancel.addActionListener {
            this.dispose()
        }
        buttons.add(cancel)

        val submit = JButton(SUBMIT)
        submit.addActionListener {
            val client = FTPClient()

            if (!address.text.startsWith(FTP_PREFIX))
                address.text = FTP_PREFIX + address.text

            try {
                client.connect(address.text.substringAfter(FTP_PREFIX))
            } catch (e: ConnectException) {
                error.reloadText("Unable to connect to the server")
                return@addActionListener
            } catch (e: Exception) {
                error.reloadText(e.localizedMessage)
                return@addActionListener
            }

            // https://stackoverflow.com/questions/10443308/why-gettext-in-jpasswordfield-was-deprecated
            // Using this library, can't get rid of this insecure line, sorry
            if (!client.login(user.text, password.text)) {
                error.reloadText("Invalid credentials. Please try again")
                return@addActionListener
            }

            client.setFileType(FTP.BINARY_FILE_TYPE)
            val app = MainWindow(FTPFileSystem(client), client)

            app.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    client.disconnect()
                }
            })

            this.dispose()
            parent.dispose()
            app.isVisible = true
        }
        buttons.add(submit)

        this.getRootPane().defaultButton = submit
        mainContainer.add(buttons, createGridBagConstraints(0, 2, 1.0, 0.0, 2))
    }
}