package ckthltm.connection;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageSender {

    public void sendMessage(Socket socket, String message) {
        try {
            System.out.println("[DEBUG] Attempting to send message: " + message);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeByte(0x01); // SEND STRING
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            out.writeInt(messageBytes.length);
            out.write(messageBytes);
            out.flush();

            System.out.println("[DEBUG] Message sent successfully. Length: " + messageBytes.length);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to send message: " + e.getMessage());
        }
    }

    public void sendFile(Socket socket, String filePath, String fileName) {
        System.out.println("[DEBUG] Bắt đầu gửi file: " + fileName);
        File file = new File(filePath);

        try (FileInputStream fis = new FileInputStream(file)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // 1. Gửi FILE NAME (Type 0x02)
            out.writeByte(0x02);
            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            out.writeInt(fileNameBytes.length); // Phải có cái này để Server readInt() không bị sai
            out.write(fileNameBytes);
            out.flush();

            // 2. Gửi FILE CHUNK (Type 0x03)
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.writeByte(0x03);
                out.writeInt(bytesRead); // Server sẽ đọc cái này vào biến 'length'
                out.write(buffer, 0, bytesRead);
                out.flush();
            }

            // 3. Gửi FILE DONE (Type 0x04)
            out.writeByte(0x04);
            out.writeInt(0); // Gửi thêm 4 byte 0 để Server readInt() xong mới vào switch-case
            out.flush();

            System.out.println("[DEBUG] Đã gửi xong toàn bộ file.");
        } catch (IOException e) {
            System.err.println("[ERROR] Lỗi khi gửi file: " + e.getMessage());
        }
    }
}
