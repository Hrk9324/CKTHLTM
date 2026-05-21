package ckthltm;

import ckthltm.connection.Server;

public class App {
    public static void main(String[] args) {
        System.setProperty("log4j2.statusLoggerLevel", "OFF");
        System.setProperty("log4j2.StatusLogger.level", "OFF");
        System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "OFF");

        Server server = new Server();
        server.start(8124);
    }
}
