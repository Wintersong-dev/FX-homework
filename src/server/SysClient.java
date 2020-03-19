package server;

public class SysClient implements Connectable {
    private final String NICKNAME = "*Сервер*";

    @Override
    public String getNickname() {
        return NICKNAME;
    }

    @Override
    public void sendMsg(Connectable sender, String msg) {
        // Ничего не делаем, это заглушка
    }
}
