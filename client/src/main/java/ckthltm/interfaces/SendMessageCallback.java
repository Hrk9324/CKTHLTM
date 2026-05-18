package ckthltm.interfaces;

public interface SendMessageCallback {
    public void sendMessage(String message);
    public void sendFile(String filePath, String fileName);
}
