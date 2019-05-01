package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerWorker extends Thread {

    private final Socket clientSocket;

    public ServerWorker(Socket clientSocket) {
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
        OutputStream outputStream = clientSocket.getOutputStream();
        InputStream inputStream = clientSocket.getInputStream();
        outputStream.write("Enter a message: ".getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null ) { // if the line is not empty
            String[] tokens = StringUtils.split(line);
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
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) {
    }
}
