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
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires org.apache.pdfbox.io;
    requires javafx.swing;

    opens org.example.bookreader to javafx.fxml, com.fasterxml.jackson.databind;
    exports org.example.bookreader;
}