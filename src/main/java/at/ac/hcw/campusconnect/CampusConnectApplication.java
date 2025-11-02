package at.ac.hcw.campusconnect;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CampusConnectApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CampusConnectApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        Image logo = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("images/logo.png"))
        );

        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("styles/main.css")).toExternalForm()
        );
        stage.getIcons().add(logo);
        stage.setTitle("CampusConnect");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}