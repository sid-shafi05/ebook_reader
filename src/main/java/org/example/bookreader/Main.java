package org.example.bookreader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/org/example/bookreader/mainscreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 5000, 4400);
        String css=this.getClass().getResource("/org/example/bookreader/application.css").toExternalForm();
        scene.getStylesheets().add(css);
        Image icon=new Image(Main.class.getResourceAsStream("/org/example/images/icon.png"));
        stage.getIcons().add(icon);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
