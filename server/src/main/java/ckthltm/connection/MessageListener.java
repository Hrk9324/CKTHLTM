package ckthltm.connection;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import ckthltm.interfaces.MessageCallback;

public class MessageListener extends Thread {
    private Socket socket;
    private String fileSavePath;
    private MessageCallback receivedCallback;

    public MessageListener(Socket socket, String fileSavePath, MessageCallback receivedCallback) {
        this.socket = socket;
        this.fileSavePath = fileSavePath;
        this.receivedCallback = receivedCallback;
    }

    @Override
    public void run() {
        FileOutputStream fos = null;
        String savedPath = null;
        String fileName = null;
        try {
            DataInputStream inp = new DataInputStream(socket.getInputStream());
            System.out.println("[SERVER] Bắt đầu lắng nghe dữ liệu từ: " + socket.getRemoteSocketAddress());

            while (true) {
                // 1. Đọc loại gói tin
                byte type = inp.readByte();

                // 2. Đọc độ dài Payload
                int length = inp.readInt();

                switch (type) {
                    case 0x01: // STRING
                        byte[] messageBytes = new byte[length];
                        inp.readFully(messageBytes);
                        String message = new String(messageBytes, StandardCharsets.UTF_8);
                        receivedCallback.onTextMessageReceived(message);
                        break;

                    case 0x02: // FILE NAME
                        byte[] fileNameBytes = new byte[length];
                        inp.readFully(fileNameBytes);
                        fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                        savedPath = fileSavePath + "/" + fileName;

                        System.out.println("[SERVER] Đang nhận file: " + fileName);

                        fos = new FileOutputStream(savedPath);
                        break;

                    case 0x03: // FILE CHUNK
                        byte[] filePayload = new byte[length];
                        inp.readFully(filePayload);
                        if (fos != null) {
                            fos.write(filePayload);
                            // Không log chunk quá nhiều để tránh rác màn hình, chỉ log khi nhận chunk
                        } else {
                            System.err.println("[SERVER] CẢNH BÁO: Nhận được chunk nhưng FileOutputStream đang null!");
                        }
                        break;

                    case 0x04: // FILE UPLOAD DONE
                        if (fos != null) {
                            fos.close();
                            fos = null;
                        }
                        if (savedPath != null) {
                            System.out.println("[SERVER] Đã nhận xong file: " + fileName);
                            receivedCallback.onFileMessageReceived(savedPath, fileName);
                        }
                        break;

                    default:
                        System.out.println("[SERVER] Gói tin không xác định: " + type + ". Bỏ qua.");
                        inp.skipBytes(length);
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("[SERVER] Kết nối bị đóng từ phía đối diện (EOF).");
        } catch (IOException e) {
            System.err.println("[SERVER] Lỗi IO: " + e.getMessage());
            // e.printStackTrace();
        }
    }
}
