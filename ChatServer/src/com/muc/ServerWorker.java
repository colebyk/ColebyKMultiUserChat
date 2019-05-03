package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private String login = null;
    private final Server server;
    private OutputStream outputStream;

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
            String[] tokens = StringUtils.split(line); // split at spaces in string
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(line)) { // equalsIgnoreCase ignores casing--could be "QUIT", "qUiT", etc
                    break;
                }else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
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

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        String msg;
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if ( (login.equals("guest") && password.equals("guest"))
                    || (login.equals("jim") && password.equals("jim")) ) {
                msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login; // sets class login string to handleLogin's login value
                System.out.println("User logged in successfully: " + login);

                String onlineMsg = login + " is now online" + "\n";

                List<ServerWorker> workerList = server.getWorkerList();
                for (ServerWorker worker : workerList) {
                    worker.send(onlineMsg);
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
} // end of ServerWorker class
