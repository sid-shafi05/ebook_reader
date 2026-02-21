package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.List;

public class CatController {

    @FXML
    private VBox categoryContainer; // ← VBox not FlowPane
    // must match fx:id in cat.fxml

    @FXML
    public void initialize() {
        loadCategories();
    }

    public void loadCategories() {
        if (categoryContainer == null) return;
        categoryContainer.getChildren().clear();

        List<String> categories = Library.getAllCategories();

        if (categories.isEmpty()) {
            Label lbl = new Label("No categories yet.\nAdd books and assign categories!");
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaaaaa; " +
                    "-fx-padding: 50; -fx-text-alignment: center;");
            categoryContainer.getChildren().add(lbl);
            return;
        }

        Controller mainCtrl = Main.getMainController();

        for (String category : categories) {
            categoryContainer.getChildren().add(
                    createCategoryTile(category, mainCtrl)
            );
        }
    }

    private VBox createCategoryTile(String category, Controller mainCtrl) {
        List<Book> books = Library.getBooksByCategory(category);

        // --- Book row (hidden by default) ---
        FlowPane row = new FlowPane();
        row.setHgap(14);
        row.setVgap(14);
        row.setStyle("-fx-padding: 4 20 18 40; -fx-background-color: transparent;");

        for (Book book : books) {
            try {
                if (mainCtrl != null)
                    row.getChildren().add(mainCtrl.createBookTile(book));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        row.setVisible(false);  // hidden on start
        row.setManaged(false);  // takes no space when hidden

        // --- Folder open/closed state ---
        boolean[] isOpen = {false};

        // --- Arrow + title ---
        Label arrow = new Label("▶");
        arrow.setStyle("-fx-text-fill: #7880a0; -fx-font-size: 12px;");

        Label catLabel = new Label(category + "  (" + books.size() + " books)");
        catLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-text-fill: #7880a0;");

        // --- Clickable header ---
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 12 20 8 20; -fx-cursor: hand; " +
                "-fx-background-color: transparent;");
        header.getChildren().addAll(arrow, catLabel);

        // --- Click = toggle open/closed ---
        header.setOnMouseClicked(e -> {
            isOpen[0] = !isOpen[0];

            if (isOpen[0]) {
                row.setVisible(true);
                row.setManaged(true);
                arrow.setText("▼");
                header.setStyle("-fx-padding: 12 20 8 20; -fx-cursor: hand; " +
                        "-fx-background-color: rgba(79,142,247,0.08);");
            } else {
                row.setVisible(false);
                row.setManaged(false);
                arrow.setText("▶");
                header.setStyle("-fx-padding: 12 20 8 20; -fx-cursor: hand; " +
                        "-fx-background-color: transparent;");
            }
        });

        // --- Hover effects ---
        header.setOnMouseEntered(e ->
                header.setStyle("-fx-padding: 12 20 8 20; -fx-cursor: hand; " +
                        "-fx-background-color: rgba(255,255,255,0.05);")
        );
        header.setOnMouseExited(e -> {
            if (!isOpen[0])
                header.setStyle("-fx-padding: 12 20 8 20; -fx-cursor: hand; " +
                        "-fx-background-color: transparent;");
        });

        // --- Divider line ---
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #1e2130;");

        // --- Wrap everything in a VBox ---
        VBox tile = new VBox();
        tile.getChildren().addAll(header, row, divider);
        return tile;
    }
}