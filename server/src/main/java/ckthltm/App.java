package ckthltm;

import ckthltm.connection.Server;

public class App {
    public static void main(String[] args) {
        Server server = new Server();
        server.start(8124);
    }
}
