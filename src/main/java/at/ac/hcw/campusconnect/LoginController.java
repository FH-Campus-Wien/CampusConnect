package at.ac.hcw.campusconnect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class LoginController {
    @FXML
    private ImageView logoImageView;

    @FXML
    public void initialize() {
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/logo.png")));
        logoImageView.setImage(logo);
    }


    public void handleSendCode(ActionEvent actionEvent) {
    }

    public void handleVerifyCode(ActionEvent actionEvent) {
    }
}