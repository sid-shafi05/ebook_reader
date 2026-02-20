package org.example.bookreader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import java.io.IOException;

public class Main extends Application {
    private static Controller mainControllerInstance;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("/org/example/bookreader/mainscreen.fxml"));

        // set controller factory so mainControllerInstance is assigned
        // BEFORE initialize() runs
        fxmlLoader.setControllerFactory(type -> {
            try {
                Object ctrl = type.getDeclaredConstructor().newInstance();
                if (ctrl instanceof Controller) {
                    mainControllerInstance = (Controller) ctrl;
                }
                return ctrl;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        mainControllerInstance = fxmlLoader.getController(); // still keep this

        String css = Main.class.getResource("/org/example/bookreader/application.css")
                .toExternalForm();
        scene.getStylesheets().add(css);

        try {
            Image icon = new Image(
                    Main.class.getResourceAsStream("/org/example/images/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Warning: Could not load icon");
        }

        stage.setTitle("StackShelf");
        stage.setScene(scene);
        stage.show();
    }
    public static Controller getMainController() {
        return mainControllerInstance;
    }
}
