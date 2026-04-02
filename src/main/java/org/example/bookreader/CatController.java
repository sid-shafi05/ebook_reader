package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.List;

public class CatController {

    @FXML
    private VBox categoryContainer;

    @FXML
    public void loadCategories() {
        if (categoryContainer == null) return;
        categoryContainer.getChildren().clear();

        List<String> categories = Library.getAllCategories();

        if (categories.isEmpty()) {
            Label lbl = new Label("No categories yet.\nAdd books and assign categories!");
            lbl.getStyleClass().add("book-card-date");
            lbl.setStyle("-fx-font-size: 16px; -fx-padding: 50; -fx-text-alignment: center;");
            categoryContainer.getChildren().add(lbl);
            return;
        }

        Controller mainCtrl = Main.getMainController();
        for (String category : categories) {
            categoryContainer.getChildren().add(createCategoryTile(category, mainCtrl));
        }
    }

    private VBox createCategoryTile(String category, Controller mainCtrl) {
        List<Book> books = Library.getBooksByCategory(category);

        // Book row — hidden by default
        FlowPane row = new FlowPane();
        row.setHgap(14);
        row.setVgap(14);
        row.getStyleClass().add("cat-book-row");

        for (Book book : books) {
            try {
                if (mainCtrl != null)
                    row.getChildren().add(mainCtrl.createBookTile(book));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        row.setVisible(false);
        row.setManaged(false);

        boolean[] isOpen = {false};

        // Arrow + title labels
        Label arrow = new Label("▶");
        arrow.getStyleClass().add("cat-arrow");

        Label catLabel = new Label(category + "  (" + books.size() + " books)");
        catLabel.getStyleClass().add("cat-title");

        // Clickable header row
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("cat-header");
        header.getChildren().addAll(arrow, catLabel);

        header.setOnMouseClicked(e -> {
            isOpen[0] = !isOpen[0];
            if (isOpen[0]) {
                row.setVisible(true);
                row.setManaged(true);
                arrow.setText("▼");
                header.getStyleClass().remove("cat-header");
                header.getStyleClass().add("cat-header-open");
            } else {
                row.setVisible(false);
                row.setManaged(false);
                arrow.setText("▶");
                header.getStyleClass().remove("cat-header-open");
                header.getStyleClass().add("cat-header");
            }
        });

        // Thin divider line
        Region divider = new Region();
        divider.getStyleClass().add("cat-divider");

        VBox tile = new VBox();
        tile.getChildren().addAll(header, row, divider);
        return tile;
    }
}