package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

/**
 * provides the backend for ChatClient
 * handles the sending of messages across the server, joining of groups, managing the list of users, etc.
 */
public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private String login = null;
    private final Server server;
    private OutputStream outputStream;

    private HashSet<String> topicSet = new HashSet<>();


    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads input from the user and, depending on the command entered, calls respective methods
     * @param clientSocket
     * @throws IOException
     * @throws InterruptedException
     */
    public void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null ) { // if the line is not empty

            /*
            Problem with StringUtils:
                downloaded apache module from apache website, implemented the commons-lang3-3.9.jar as a dependency
                in  File -> Project Structure -> Modules -> Dependencies
            */
            String[] tokens = StringUtils.split(line); // split at spaces in string and puts each word into tokens array
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0]; // first word in tokens array is the command
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(line)) { // equalsIgnoreCase ignores casing--could be "QUIT", "qUiT", etc
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    handleMessage(tokens);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());

                }
            }
        }

        clientSocket.close();
    } // end of handleClientSocket method

    /**
     *
     * @return the current user's username
     */
    public String getLogin() {
        return login;
    }

    /**
     * removes the user from the list of connected users
     * send a notification to each connected user that the current user went offline
     * @throws IOException
     */
    private void handleLogoff() throws IOException {
        server.removeWorker(this); // remove this instance of serverWorker

        List<ServerWorker> workerList = server.getWorkerList();

        // send other online users current user's status
        String onlineMsg = null;
        for (ServerWorker worker : workerList) { // for each instance of worker in the workerList arraylist (everyone connected)
            if (!login.equals(worker.getLogin())) { // if the user in workerList is the login just used, don't send the message
                onlineMsg = "offline " + login + "\n";
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    /**
     * checks if the entered login information is correct
     * notifies each user that someone logged in
     * gives the user logging in a list of everyone already connected
     * prints an error message if login information is incorrect
     * @param outputStream
     * @param tokens
     * @throws IOException
     */
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        String msg;
        if (tokens.length == 3) { // make sure the command *login* has only 3 words
            String login = tokens[1]; // set the second word to the username
            String password = tokens[2]; // set the third word to the password

            if ( (login.equals("guest") && password.equals("guest"))
                    || (login.equals("jim") && password.equals("jim")) ) { // if the login is guest or jim
                msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login; // sets class login string to handleLogin's login value
                System.out.println("User logged in successfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();

                // send current user all other online people
                for (ServerWorker worker : workerList) { // for each instance of worker in the workerList arraylist (everyone connected)
                    if (worker.getLogin() != null) { // if one of the users connected is null (hasn't logged in), don't send the message
                        if (!login.equals(worker.getLogin())) { // if a user in workerList matches the login just used, don't send the message
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg;
                for (ServerWorker worker : workerList) { // for each instance of worker in the workerList arraylist (everyone connected)
                    if (!login.equals(worker.getLogin())) { // if the user in workerList is the login just used, don't send the message
                        onlineMsg = "online " + login + "\n";
                        worker.send(onlineMsg);
                    }
                }

            } else {
                msg = "error in login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }

        }
    }  // end of handleLogin method

    /**
     * handles sending a message to intended users
     * @param msg
     * @throws IOException
     */
    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }

    /**
     * constructs the message based on passed in tokens
     * checks if the message is directed towards a group chat
     * sends the message to the correct user
     * @param tokens
     * @throws IOException
     */
    // format: "msg" <user> text...
    // alt format: "msg" <#topic> text...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = "";

        boolean isInTopic = sendTo.charAt(0) == '#';
        for (int i = 2; i < tokens.length; i++) { // for every "word" in tokens
            body = body.concat(" " + tokens[i]); // add the word to the message body
        }

        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isInTopic) { // if the message is intended for a group chat
                if (worker.isMemberOfTopic(sendTo)) { // if the worker is in that group chat
                    String outMsg = "from " + login + ", in " + sendTo + ":" + body + "\n";
                    worker.send(outMsg);
                }
            }
            if (sendTo.equalsIgnoreCase(worker.getLogin())) { // if the user enters a valid name of someone connected, send the message
                String outMsg = "msg " + login + " " + body + "\n";
                worker.send(outMsg);
            }
        }

    }

    /**
     *
     * @param topic
     * @return true or false, depending on if a user is part of a certain group chat
     */
    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic); // if the user is part of the "group chat", return true
    }

    /**
     * add the user to a group chat based on String[] tokens
     * @param tokens
     */
    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic); // add the group the user joined to the list of groups
        }
    }

    /**
     * remove the user from a group chat based on String[] tokens
     * @param tokens
     */
    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic); // remove a group the user has joined from the list of groups
        }
    }
} // end of ServerWorker class
