package server;

public interface Connectable {
    public String getNickname();

    public void sendMsg(Connectable sender, String msg);
}
