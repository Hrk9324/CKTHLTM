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
            System.out.println("[CLIENT] Bắt đầu lắng nghe dữ liệu từ: " + socket.getRemoteSocketAddress());

            while (true) {
                // 1. Đọc loại gói tin
                byte type = inp.readByte();
                System.out.println("[CLIENT] Đã nhận Byte Type: 0x" + String.format("%02X", type));

                // 2. Đọc độ dài Payload
                int length = inp.readInt();
                System.out.println("[CLIENT] Độ dài Payload nhận được: " + length + " bytes");

                switch (type) {
                    case 0x01: // STRING
                        byte[] messageBytes = new byte[length];
                        inp.readFully(messageBytes);
                        String message = new String(messageBytes, StandardCharsets.UTF_8);
                        System.out.println("[CLIENT] Nội dung tin nhắn: " + message);
                        receivedCallback.onTextMessageReceived(message);
                        break;

                    case 0x02: // FILE NAME
                        byte[] fileNameBytes = new byte[length];
                        inp.readFully(fileNameBytes);
                        fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                        savedPath = fileSavePath + "/" + fileName;

                        System.out.println("[CLIENT] Đang chuẩn bị nhận file: " + fileName);
                        System.out.println("[CLIENT] Đường dẫn lưu: " + savedPath);

                        fos = new FileOutputStream(savedPath);
                        break;

                    case 0x03: // FILE CHUNK
                        byte[] filePayload = new byte[length];
                        inp.readFully(filePayload);
                        if (fos != null) {
                            fos.write(filePayload);
                            // Không log chunk quá nhiều để tránh rác màn hình, chỉ log khi nhận chunk
                        } else {
                            System.err.println("[CLIENT] CẢNH BÁO: Nhận được chunk nhưng FileOutputStream đang null!");
                        }
                        break;

                    case 0x04: // FILE UPLOAD DONE
                        System.out.println("[CLIENT] Nhận được tín hiệu kết thúc file (0x04)");
                        if (fos != null) {
                            fos.close();
                            fos = null;
                            System.out.println("[CLIENT] Đã đóng file stream.");
                        }
                        if (savedPath != null) {
                            System.out.println("[CLIENT] Gọi callback xử lý file sau khi nhận xong...");
                            receivedCallback.onFileMessageReceived(savedPath, fileName);
                        }
                        break;

                    default:
                        System.out.println(
                                "[CLIENT] Type không xác định: " + type + ". Đang bỏ qua " + length + " bytes.");
                        inp.skipBytes(length);
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("[CLIENT] Kết nối bị đóng từ phía đối diện (EOF).");
        } catch (IOException e) {
            System.err.println("[CLIENT] Lỗi IO: " + e.getMessage());
            // e.printStackTrace();
        }
    }
}
