package org.example.bookreader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Controller mainControllerInstance;
    private static Stage primaryStage;  // stored so any controller can reach it

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("/org/example/bookreader/mainscreen.fxml"));

        // Set controller factory so mainControllerInstance is assigned BEFORE
        // initialize() runs, fixing the null reference on startup.
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
        mainControllerInstance = fxmlLoader.getController();

        String css = Main.class.getResource("/org/example/bookreader/application.css")
                .toExternalForm();
        scene.getStylesheets().add(css);

        try {
            Image icon = new Image(Main.class.getResourceAsStream("/org/example/images/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Warning: Could not load app icon");
        }

        stage.setTitle("StackShelf");
        stage.setScene(scene);
        stage.show();
    }

    public static Controller getMainController() {
        return mainControllerInstance;
    }

    public static void setMainController(Controller c) {
        mainControllerInstance = c;
    }

    /** Always returns the real primary stage — never null */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}