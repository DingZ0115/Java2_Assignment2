module com.example.assignment2_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;


    opens com.example.assignment2_client to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.example.assignment2_client;
}