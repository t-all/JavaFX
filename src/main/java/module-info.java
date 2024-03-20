module com.example.fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires docx4j.core;
    requires javafx.web;
    requires java.logging;
    requires java.desktop;
    requires javafx.graphics;

    opens com.example.fx to javafx.fxml;
    exports com.example.fx;
}