package ckthltm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ckthltm.connection.Client;

public class App {
    public static void main(String[] args) {
        Client client = new Client();
        try {
            InetAddress address = InetAddress.getLocalHost();
            client.connect(address, 8124);
        } catch (UnknownHostException e) {
            System.err.println(e);
        }
    }
}
