package swing.window

import observer.filesystem.FTPFileSystem
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import swing.*
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.*
import java.net.ConnectException
import javax.swing.*


class FTPSettingsWindow(header: String, private val parent: JFrame) : JFrame(header) {
    private val address = JTextField(FTP_PREFIX)
    private val anonymous = JCheckBox("Anonymous connection")

    private val userLabel = JLabel("User")
    private val user = JTextField()

    private val passwordLabel = JLabel("Password")
    private val password = JPasswordField()

    private val error = JTextArea()

    init {
        this.size = Dimension(350, 220)
        this.setLocationRelativeTo(null)

        val mainContainer = this.contentPane
        mainContainer.layout = GridBagLayout()

        val settings = JPanel()
        settings.layout = GridBagLayout()

        settings.add(JLabel("Server address"), createGridBagConstraints(0, 0, 0.0, 0.0))
        settings.add(address, createGridBagConstraints(1, 0, 1.0, 0.0))

        anonymous.addItemListener(AnonymousSelection(this))
        anonymous.name = ANONYMOUS_CHECKBOX
        settings.add(anonymous, createGridBagConstraints(0, 1, 1.0, 0.0, 2))

        userLabel.name = USERNAME_LABEL
        settings.add(userLabel, createGridBagConstraints(0, 2, 0.0, 0.0))
        user.name = USERNAME_TEXT_FIELD
        settings.add(user, createGridBagConstraints(1, 2, 1.0, 0.0))

        settings.add(passwordLabel, createGridBagConstraints(0, 3, 0.0, 0.0))
        settings.add(password, createGridBagConstraints(1, 3, 1.0, 0.0))

        mainContainer.add(settings, createGridBagConstraints(0, 0, 1.0, 1.0))
        error.initErrorField()
        mainContainer.add(error, createGridBagConstraints(0, 1, 1.0, 0.0, 2))

        val buttons = JPanel()
        buttons.layout = GridLayout(1, 2)

        val cancel = JButton(CANCEL)
        cancel.name = CANCEL_BUTTON
        cancel.addActionListener { this.dispose() }
        buttons.add(cancel)

        val submit = JButton(SUBMIT)
        submit.addActionListener(Connection(this))
        buttons.add(submit)

        this.getRootPane().defaultButton = submit
        mainContainer.add(buttons, createGridBagConstraints(0, 2, 1.0, 0.0, 2))
    }


    private class AnonymousSelection(private val parent: FTPSettingsWindow) : ItemListener {
        override fun itemStateChanged(it: ItemEvent) {
            when (it.stateChange) {
                ItemEvent.SELECTED -> {
                    parent.userLabel.isEnabled = false
                    parent.user.isEnabled = false
                    parent.passwordLabel.isEnabled = false
                    parent.password.isEnabled = false

                    parent.user.text = "anonymous"
                    parent.password.text = ""
                }
                ItemEvent.DESELECTED -> {
                    parent.userLabel.isEnabled = true
                    parent.user.isEnabled = true
                    parent.passwordLabel.isEnabled = true
                    parent.password.isEnabled = true

                    parent.user.text = ""
                }
            }
        }
    }

    private class Connection(private val parent: FTPSettingsWindow) : ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {

            val client = FTPClient()

            if (!parent.address.text.startsWith(FTP_PREFIX))
                parent.address.text = FTP_PREFIX + parent.address.text

            try {
                // sometimes it is really time consuming (I guess client.login is the same)
                // should be done in other thread
                client.connect(parent.address.text.substringAfter(FTP_PREFIX))
            } catch (e: ConnectException) {
                parent.error.reloadText("Unable to connect to the server")
                return
            } catch (e: Exception) {
                parent.error.reloadText(e.localizedMessage)
                return
            }

            // https://stackoverflow.com/questions/10443308/why-gettext-in-jpasswordfield-was-deprecated
            // Using this library, can't get rid of this insecure line, sorry
            if (!client.login(parent.user.text, parent.password.text)) {
                parent.error.reloadText(
                    if (parent.anonymous.isSelected) "Unable to connect as anonymous"
                    else "Invalid credentials. Please try again"
                )
                return
            }

            client.setFileType(FTP.BINARY_FILE_TYPE)
            val app = MainWindow(FTPFileSystem(client), client)

            app.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    client.disconnect()
                }
            })

            parent.dispose()
            parent.parent.dispose()
            app.isVisible = true
        }
    }

    private companion object {
        const val FTP_PREFIX = "ftp://"
    }
}