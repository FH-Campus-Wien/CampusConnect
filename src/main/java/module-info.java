module at.ac.hcw.campusconnect {
    requires javafx.controls;
    requires javafx.fxml;


    opens at.ac.hcw.campusconnect to javafx.fxml;
    exports at.ac.hcw.campusconnect;
}