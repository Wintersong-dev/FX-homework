package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField msgField;

    private final String IP_ADDR = "localhost";
    private final int PORT = 9876;
    private boolean isAuth = false;
    private String nickname;
    private final String NO_AUTH = "Chat lobby";

    private Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public void setAuth(boolean _auth) {
        isAuth = _auth;
        authPanel.setVisible(!_auth);
        authPanel.setManaged(!_auth);
        msgPanel.setVisible(_auth);
        msgPanel.setManaged(_auth);
        if (!_auth) {
            setNickname("");
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        setTitle(nickname);
    }

    public void auth() {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText().trim() + " " + passwordField.getText().trim());
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg()  {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setTitle(NO_AUTH);
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDR, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                String str;
                try {
                    while (true) {
                        str = in.readUTF();
                        System.out.println(str);
                        if (str.startsWith("/authok ")) {
                            setNickname(str.split(" ")[1]);
                            setAuth(true);
                            break;
                        }
                        textArea.setText(str);
                    }

                    Platform.runLater(() -> {
                        textArea.clear();
                        msgField.requestFocus();
                    });

                    while (true) {
                        receiveMsg();
                    }
                } catch (IOException e) {
                    try {
                        socket.close();
                        setAuth(false);
                    } catch (IOException ignore) {}
                }

            }).start();


        } catch (IOException e) {
            setAuth(false);
            try {
                socket.close();
                setTitle(NO_AUTH);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void receiveMsg() throws IOException {
        String str = in.readUTF();
        textArea.appendText(str + "\n");
    }

    void setTitle(String title) {
        Platform.runLater(() -> {
            ((Stage)textArea.getScene().getWindow()).setTitle(title);
        });
    }

}
