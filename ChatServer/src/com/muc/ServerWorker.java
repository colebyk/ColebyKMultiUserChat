package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

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

    public void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
        this.outputStream = clientSocket.getOutputStream();
        InputStream inputStream = clientSocket.getInputStream();

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


    public String getLogin() {
        return login;
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this); // remove this instance of serverWorker

        List<ServerWorker> workerList = server.getWorkerList();

        // send other online users current user's status
        String onlineMsg = null;
        for (ServerWorker worker : workerList) { // for each instance of worker in the workerList arraylist (everyone connected)
            if (!login.equals(worker.getLogin())) { // if the user in workerList is the login just used, don't send the message
                onlineMsg = "\n" + login + " is now offline" + "\n";
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

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
                            String msg2 = "\n" + worker.getLogin() + " is online" + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg = null;
                for (ServerWorker worker : workerList) { // for each instance of worker in the workerList arraylist (everyone connected)
                    if (!login.equals(worker.getLogin())) { // if the user in workerList is the login just used, don't send the message
                        onlineMsg = "\n" + login + " is now online" + "\n";
                        worker.send(onlineMsg);
                    }
                }

            } else {
                msg = "error in login\n";
                outputStream.write(msg.getBytes());
            }

        }
    }  // end of handleLogin method

    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }

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
            if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = "from " + login + ":" + body + "\n";
                worker.send(outMsg);
            }
        }

    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic); // if the user is part of the "group chat", return true
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }
} // end of ServerWorker class
