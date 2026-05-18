package ckthltm.interfaces;

public interface MessageCallback {
    public void onTextMessageReceived(String message);
    public void onFileMessageReceived(String savedPath, String fileName);
}
