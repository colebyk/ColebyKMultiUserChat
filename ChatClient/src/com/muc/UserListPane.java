package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener {


    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this); // add a user status listener to the user list pane

        userListModel = new DefaultListModel<>(); // model for listing Strings
        userListUI = new JList<>(userListModel); // construct a list in the form of DefaultListModel
        setLayout(new BorderLayout()); // set the layout of the JPanel
        add(new JScrollPane(userListUI), BorderLayout.CENTER); // add a scrolling pane to the JPanel in the center

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) { // if the user clicked a list element more than once (double clicked)...
                    String login = userListUI.getSelectedValue(); // set login to the name of the person double clicked
                    MessagePane messagePane = new MessagePane(client, login); // construct a MessagePane

                    JFrame f = new JFrame("Message: " + login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500,500);
                    f.getContentPane().add(messagePane,BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });

    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8818); // make a new client instance
        UserListPane userListPane = new UserListPane(client); // make a new userListPane instance
        JFrame frame = new JFrame("User List"); // make a new JFrame instance
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,600);

        frame.getContentPane().add(new JScrollPane(userListPane), BorderLayout.CENTER); // add a JScrollPane to the JFrame
        frame.setVisible(true);

        if (client.connect()) { // if the client can connect to the server
            try {
                client.login("guest", "guest"); // login to the server
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void online(String login) {
        userListModel.addElement(login); // add the user's login to the list
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login); // remove the user's login to the list

    }
}
