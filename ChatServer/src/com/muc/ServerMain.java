package com.muc;

/**
 * makes a new server
 * runs the multi user chat
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = 8818;
        Server server = new Server(port);
        server.start(); // starts a new thread
    }
}