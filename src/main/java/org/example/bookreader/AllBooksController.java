package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AllBooksController {

    @FXML private FlowPane bookGrid;

    private String currentSort   = "title";
    private String currentFilter = "";

    @FXML
    public void initialize() {
        loadBooks();
    }

    public void setSort(String sort) {
        this.currentSort = sort;
        loadBooks();
    }

    public void filterBooks(String query) {
        this.currentFilter = query;
        loadBooks();
    }

    public void loadBooks() {
        if (bookGrid == null) return;
        bookGrid.getChildren().clear();

        List<Book> books = new ArrayList<>(BookDatabase.getInstance().getAllBooks());

        // Filter
        if (!currentFilter.isEmpty()) {
            books = books.stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(currentFilter)
                            || b.getAuthorName().toLowerCase().contains(currentFilter)
                            || b.getCategory().toLowerCase().contains(currentFilter))
                    .collect(Collectors.toList());
        }

        // Sort
        switch (currentSort) {
            case "author":
                books.sort(Comparator.comparing(b -> b.getAuthorName().toLowerCase()));
                break;
            case "date":
                books.sort(Comparator.comparingLong(Book::getDateAdded).reversed());
                break;
            case "progress":
                books.sort(Comparator.comparingDouble(Book::getProgress).reversed());
                break;
            default: // title
                books.sort(Comparator.comparing(b -> b.getTitle().toLowerCase()));
        }

        if (books.isEmpty()) {
            String msg = currentFilter.isEmpty()
                    ? "No books yet. Click 'Add Book' to get started!"
                    : "No books match \"" + currentFilter + "\"";
            Label lbl = new Label(msg);
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaaaaa; -fx-padding: 50;");
            bookGrid.getChildren().add(lbl);
            return;
        }

        // Get the main Controller to use its createBookTile method (consistent UI)
        for (Book book : books) {
            // Lookup mainCtrl at tile-creation time so it's always fresh
            Controller mainCtrl = Main.getMainController();
            VBox tile = (mainCtrl != null)
                    ? mainCtrl.createBookTile(book)
                    : fallbackTile(book);
            bookGrid.getChildren().add(tile);
        }
    }

    /** Simple fallback tile if main controller isn't available yet */
    private VBox fallbackTile(Book book) {
        VBox tile = new VBox(5);
        tile.getStyleClass().add("book-card");
        tile.getChildren().add(new Label(book.getTitle()));
        tile.setOnMouseClicked(e -> {
            if (Main.getMainController() != null)
                Main.getMainController().openBook(book);
        });
        return tile;
    }

    public void refreshBooks() {
        loadBooks();
    }
}