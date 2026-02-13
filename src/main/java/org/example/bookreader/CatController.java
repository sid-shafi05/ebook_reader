package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

public class CatController {

    @FXML private VBox categoryContainer;

    @FXML
    public void initialize() {
        loadCategories();
    }

    public void loadCategories() {
        if (categoryContainer == null) return;
        categoryContainer.getChildren().clear();

        List<String> categories = BookDatabase.getInstance().getAllCategories();

        if (categories.isEmpty()) {
            Label lbl = new Label("No categories yet.\nAdd books and assign categories to see them here!");
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaaaaa; -fx-padding: 50; -fx-text-alignment: center;");
            categoryContainer.getChildren().add(lbl);
            return;
        }

        Controller mainCtrl = Main.getMainController();

        for (String category : categories) {
            List<Book> books = BookDatabase.getInstance().getBooksByCategory(category);
            if (books.isEmpty()) continue;

            // Folder header
            HBox header = new HBox(8);
            header.setStyle("-fx-padding: 16 20 8 20; -fx-alignment: center-left; -fx-background-color: transparent;");
            Label folderIcon = new Label("Folder:");
            folderIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3f54;");
            Label catLabel = new Label(category + "  (" + books.size() + " books)");
            catLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #7880a0;");
            header.getChildren().addAll(folderIcon, catLabel);

            // Book row
            FlowPane row = new FlowPane();
            row.setHgap(14);
            row.setVgap(14);
            row.setStyle("-fx-padding: 4 20 18 40; -fx-background-color: transparent;");
            row.setPrefWrapLength(900);

            for (Book book : books) {
                if (mainCtrl != null) row.getChildren().add(mainCtrl.createBookTile(book));
            }

            // Separator
            Region sep = new Region();
            sep.setPrefHeight(1);
            sep.setStyle("-fx-background-color: #1e2130;");

            categoryContainer.getChildren().addAll(header, row, sep);
        }
    }
}