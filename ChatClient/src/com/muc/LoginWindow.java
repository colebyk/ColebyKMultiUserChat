package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame {

    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");


    public LoginWindow() {
        super("Login"); // call JFrame Constructor using super() and set its title to Login

        this.client = new ChatClient("localhost", 8818);
        client.connect();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(900,500);
        JPanel p = new JPanel();
        //p.setSize(500,600);

        //loginField.setSize(100, 10);
        //passwordField.setSize(100, 10);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);

        pack(); // resize window to fit components

        setVisible(true);
    }

    private void doLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        try {
            if (client.login(login, password)) {

                // bring up the user list window
                UserListPane userListPane = new UserListPane(client); // make a new userListPane instance
                JFrame frame = new JFrame("User List"); // make a new JFrame instance
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400,600);

                frame.getContentPane().add(new JScrollPane(userListPane), BorderLayout.CENTER); // add a JScrollPane to the JFrame
                frame.setVisible(true);

                setVisible(false); // hide the login window
            } else {
                // show error message
                JOptionPane.showMessageDialog(this, "Invalid login / password");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.setVisible(true);

    }
}
