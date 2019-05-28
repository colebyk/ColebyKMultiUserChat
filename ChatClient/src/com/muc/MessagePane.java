package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * provides the GUI for messaging other users
 */
public class MessagePane extends JPanel implements MessageListener {

    private final ChatClient client;
    private final String login;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String login) {

        this.client = client;
        this.login = login;

        client.addMessageListener(this); // add a message listener to the message pane (this)

        setLayout(new BorderLayout());

        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() { // add a listener for the text entering box
            @Override
            public void actionPerformed(ActionEvent e) { // when an action is performed (enter pressed)...
                try {
                    String text = inputField.getText(); // set text to what the user has typed
                    client.msg(login, text); // message the determined user
                    listModel.addElement("You: " + text); // add the sent message to the list in the GUI
                    inputField.setText(""); // empty the text box
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        if (login.equalsIgnoreCase(fromLogin)) { // if the message received is from the user currently open in the message pane
            String line = fromLogin + ": " + msgBody;
            listModel.addElement(line); // add the incoming message to the message pane
        }
    }
}
