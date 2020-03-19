package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;
    Connectable systemUser = new SysClient();

    final int PORT = 9876;

    Server() {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;
        authService = new SimpleAuth();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            while (true) {
                socket = server.accept();
                System.out.println("Новый клиент!");
                new ClientHandler(socket, this);
            }

        // Сервер упал
        } catch (IOException e) {
            for (ClientHandler client : clients) {
                client.sendMsg(systemUser, "Неполадки на сервере, отключение...");
                client.disconnect();
            }
            try {
                socket.close();
            } catch (Exception ignore) {}
            try {
                server.close();
            } catch (Exception ignore) {}

        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    void addClient(ClientHandler client) {
        clients.add(client);
    }

    void removeClient(ClientHandler client) {
        try {
            clients.remove(client);
        } catch (Exception ignore) {}
    }

    // Отправка на всех (не более 1 исключения, в качестве исключения во избежание NPE обычно передается псевдопользователь systemUser)
    void broadcast(Connectable sender, String msg, Connectable ignored) {
        for (ClientHandler client : clients) {
            try {
                if (!client.getNickname().equals(ignored.getNickname())) {
                    client.sendMsg(sender, msg);
                }
            } catch (NullPointerException ignore) {}

        }
    }

    // Отправка ЛС
    void privateMessage(Connectable sender, Connectable receiver, String msg) {
        sender.sendMsg(sender, receiver.getNickname() + ", " + msg);
        receiver.sendMsg(sender,receiver.getNickname() + ", " + msg);
    }

    // Получаем клиента по его нику
    ClientHandler getClientByNick(String nick) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nick)) {
                    return client;
            }
        }

        // Клиент не найден
        return null;
    }
}
