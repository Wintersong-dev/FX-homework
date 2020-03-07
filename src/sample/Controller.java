package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class Controller {
    @FXML
    private TextArea textArea;
    @FXML
    private TextField inputField;

    public void send(String msg) {
        textArea.appendText(msg + "\n");
        inputField.setText("");
    }

    public void btnClick(ActionEvent e) {
        if (inputField.getText() != null) {
            send(inputField.getText());
        }

    }

    public void textFieldUpdated(KeyEvent e) {
        if (e.getCode().equals(KeyCode.ENTER) && inputField.getText() != null) {
            send(inputField.getText());
        }
    }
}
