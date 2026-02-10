package org.example.bookreader;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import java.io.IOException;

public class AllBookController {
    @FXML private FlowPane bookContainer;
   //deleted the changeToAllBooks(), it is already in Controller.java
   @FXML
   public void initialize() {
       System.out.println("DEBUG: AllBookController is waking up!");
       if (bookContainer == null) {
           System.out.println("DEBUG: ERROR! bookContainer is NULL. Check fx:id in SceneBuilder!");
           return;
       }
       if (bookContainer != null) {
           bookContainer.getChildren().clear();
       }

       List<Book> mySavedBooks = Library.loadBooks();
       System.out.println("DEBUG: Found " + mySavedBooks.size() + " books in JSON.");

       for (Book book : mySavedBooks) {
           // 1. Create the container for one book "card"
           VBox bookUI = new VBox(10); // 10 is the spacing between elements
           bookUI.setAlignment(Pos.CENTER);
           bookUI.setStyle("-fx-padding: 10; -fx-background-color: #2d2d2d; -fx-background-radius: 8;");

           // 2. CREATE THE IMAGEVIEW (The Cover)
           ImageView coverView = new ImageView();
           coverView.setFitHeight(150); // Set a standard height for covers
           coverView.setFitWidth(100);
           coverView.setPreserveRatio(true);

           // Logic: Load the image from the book's coverPath
           // We use the file path we saved when adding the book
           if (book.getCoverPath() != null) {
               File imageFile = new File(book.getCoverPath());
               if (imageFile.exists()) {
                   Image coverImage = new Image(imageFile.toURI().toString());
                   coverView.setImage(coverImage);
               }
           }

           // 3. Create the Title Label
           Label title = new Label(book.getTitle());
           title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
           title.setWrapText(true); // Ensures long titles don't go off-screen
           title.setMaxWidth(100);

           // 4. THE HANDOFF: Add the Image and the Title to the VBox
           bookUI.getChildren().addAll(coverView, title);

           // 5. THE WIRING: Clicking the whole VBox opens the reader
           bookUI.setOnMouseClicked(event -> {
               Main.getMainController().openReader(book);
           });

           // Add this finished "card" to the purple FlowPane
           bookContainer.getChildren().add(bookUI);
       }
   }
}

