package ckthltm.connection;

import java.io.File;
import java.util.Scanner;

import ckthltm.interfaces.SendMessageCallback;

public class UI {
    private SendMessageCallback sendCallback;

    public UI(SendMessageCallback sendCallback) {
        this.sendCallback = sendCallback;
    }

    public class InputReader extends Thread {
        @Override
        public void run() {
            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    System.out.println("\n--- CHOOSE ACTION ---");
                    System.out.println("1. Upload Excel File");
                    System.out.println("2. Send Parameters (n, m, L)");
                    System.out.print("Your choice: ");
                    String choice = sc.nextLine();

                    if (choice.equals("1")) {
                        System.out.print("Enter file path: ");
                        String path = sc.nextLine();
                        File file = new File(path);
                        if (file.exists()) {
                            sendCallback.sendFile(path, file.getName());
                        } else {
                            System.out.println("File not found!");
                        }
                    } else if (choice.equals("2")) {
                        System.out.print("Enter parameters (format: n=..., m=..., L=...): ");
                        String params = sc.nextLine();
                        sendCallback.sendMessage(params);
                    }
                }
            }
        }
    }

    public void display() {
        new InputReader().start();
    }
}
