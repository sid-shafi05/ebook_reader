module org.example.bookreader {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires javafx.swing;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    // opens to javafx.fxml for FXML injection
    // opens to jackson.databind for JSON serialization (bro's addition - needed!)
    opens org.example.bookreader to javafx.fxml, com.fasterxml.jackson.databind;
    exports org.example.bookreader;
}