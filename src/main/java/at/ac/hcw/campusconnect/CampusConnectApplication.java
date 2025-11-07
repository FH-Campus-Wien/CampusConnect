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
import java.util.prefs.Preferences;

public class CampusConnectApplication extends Application {
    private static final Preferences prefs = Preferences.userRoot().node("at.ac.hcw.campusconnect");

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
        Scene scene = new Scene(fxmlLoader.load());
        Image logo = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("images/logo.png"))
        );

        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("styles/main.css")).toExternalForm()
        );

        stage.getIcons().add(logo);
        stage.setTitle("CampusConnect");

        // restore window size
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.setWidth(prefs.getDouble("window.width", 800));
        stage.setHeight(prefs.getDouble("window.height", 600));

        stage.setOnCloseRequest(e -> {
            prefs.putDouble("window.width", stage.getWidth());
            prefs.putDouble("window.height", stage.getHeight());
        });

        stage.setScene(scene);
        stage.show();
    }
}