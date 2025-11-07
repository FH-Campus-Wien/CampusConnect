package at.ac.hcw.campusconnect;

import at.ac.hcw.campusconnect.config.SupabaseConfig;
import at.ac.hcw.campusconnect.services.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CampusConnectApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        SupabaseConfig.initialize();

        SessionManager sessionManager = SessionManager.getInstance();
        SessionManager.SessionState sessionState = sessionManager.initializeSession();

        String fxmlFile;
        switch (sessionState) {
            case AUTHENTICATED_WITH_PROFILE:
                // TODO: Load main app screen when implemented
                fxmlFile = "login.fxml"; // For now, default to login
                break;
            case AUTHENTICATED_NEEDS_PROFILE:
                fxmlFile = "profile-setup.fxml";
                break;
            case NEEDS_LOGIN:
            default:
                fxmlFile = "login.fxml";
                break;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(CampusConnectApplication.class.getResource(fxmlFile));
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
}