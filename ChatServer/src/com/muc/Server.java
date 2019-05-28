package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * creates a new thread and socket for each connecting user
 */
public class Server extends Thread {
    private final int serverPort;

    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * @return the list of users
     */
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket); // *this* refers to this Server object (this class itself)
                workerList.add(worker); // add this user to an arraylist of workers
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param serverWorker
     * remove a user from the arraylist of current connected users
     */
    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker); // remove the passed in instance of serverWorker from the workerList array list
    }
}
