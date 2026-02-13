package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FavController {

    @FXML private FlowPane favGrid;

    @FXML
    public void initialize() {
        loadFavourites();
    }

    public void loadFavourites() {
        if (favGrid == null) return;
        favGrid.getChildren().clear();

        List<Book> favBooks = BookDatabase.getInstance().getFavouriteBooks();

        if (favBooks.isEmpty()) {
            Label lbl = new Label("No favourites yet.\nClick the heart on any book to add it here!");
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaaaaa; -fx-padding: 50; -fx-text-alignment: center;");
            favGrid.getChildren().add(lbl);
            return;
        }

        Controller mainCtrl = Main.getMainController();
        for (Book book : favBooks) {
            VBox tile = (mainCtrl != null) ? mainCtrl.createBookTile(book) : new VBox();
            favGrid.getChildren().add(tile);
        }
    }
}