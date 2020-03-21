package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Connectable {
    Server server;
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    private String nick;
    String login;

    private final int MSG_COMMON = 0;   // Обычное сообщение
    private final int MSG_AUTH = 1;     // Зарос на авторизацию
    private final int MSG_END = 2;      // Запрос на выход из чата
    private final int MSG_WHISPER = 3;  // Запрос на личное сообщение

    ClientHandler(Socket _socket, Server _server) {
        server = _server;
        socket = _socket;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            new Thread(() -> {
                String[] tokens;
                String str;
                try {
                    // Авторизация
                    while (true) {
                        str = in.readUTF();

                        // Ловим сообщение на авторизацию...
                        if (parseMessage(str) == MSG_AUTH) {
                            tokens = str.split(" ");
                            String newNick = server.getAuthService().getNickname(tokens[1], tokens[2]);

                            // Успех!
                            if (newNick != null) {
                                server.addClient(this);
                                nick = newNick;
                                login = tokens[0];

                                // Отправляем клиенту его ник. Здесь отправитель роли не играет
                                sendMsg(this, "/authok " + nick);
                                System.out.println("Клиент " + nick + " прошел аутентификацию");

                                // Всем кроме себя сообщаем о своем входе в чат
                                server.broadcast(server.systemUser, nick + " вошел в чат", this);

                                // А потом отправляем себе приветствие
                                sendMsg(server.systemUser, "Добро пожаловать в чат, " + nick);
                                break;

                            // Неудача!
                            } else {
                                sendMsg(server.systemUser, "Неверный логин/пароль");
                            }
                        }
                    }

                    // Рабочий цикл чата
                    while (true) {
                        str = in.readUTF();

                        // Парсим пришедшее сообщшние
                        switch (parseMessage(str)) {
                            // Запрос на выход из чата
                            case MSG_END:
                                disconnect();
                                break;
                            // Запрос на личное сообщение
                            case MSG_WHISPER:
                                tokens = str.split(" ");
                                server.privateMessage(this, server.getClientByNick(tokens[1]), tokens[2]);
                                break;
                            // Обычное сообщение
                            case MSG_COMMON:
                                server.broadcast(this, str, server.systemUser);
                                break;
                        }

                        // Проверяем, не вышли ли мы еще из чата
                        if (socket.isClosed()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            disconnect();
        }
    }

    @Override
    public void sendMsg(Connectable sender, String msg) {
        try {
            if (msg.startsWith("/")) {
                out.writeUTF(msg);
            } else {
                out.writeUTF(sender.getNickname() + ": " + msg);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void disconnect() {
        try {
            // Прощаемся
            sendMsg(server.systemUser, "Отключение...");

            // Уведомляем остальных
            server.broadcast(server.systemUser, nick + " вышел из чата", this);

            // Подчищаем за клиентом
            socket.close();
            server.removeClient(this);
            System.out.println(this.nick + " ушел");
        } catch (IOException ignore) {}
    }

    @Override
    public String getNickname() {
        return nick;
    }

    private int parseMessage(String msg) {
        int res = MSG_COMMON; // По умолчанию обычное сообщение

        if (msg.startsWith("/auth ")) { // Зарос на авторизацию
            res = MSG_AUTH;
        } else if (msg.equals("/end")){ // Запрос на выход из чата
            res = MSG_END;
        } else if (msg.startsWith("/w ")) { // Запрос на личное сообщение
            res = MSG_WHISPER;
        }

        return res;
    }
}
