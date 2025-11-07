module at.ac.hcw.campusconnect {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.prefs;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires static lombok;

    opens at.ac.hcw.campusconnect to javafx.fxml;
    opens at.ac.hcw.campusconnect.models to com.fasterxml.jackson.databind;
    exports at.ac.hcw.campusconnect;
    exports at.ac.hcw.campusconnect.controller;
    opens at.ac.hcw.campusconnect.controller to javafx.fxml;
}