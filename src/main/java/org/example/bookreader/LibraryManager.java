package org.example.bookreader;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class LibraryManager {
    private FlowPane bookGrid;
    private FlowPane favGrid;
    private List<Book> bookList;
    private String currentQuery = "";
    private Controller controller;

    public LibraryManager(FlowPane bookGrid, FlowPane favGrid,
                          List<Book> bookList, Controller controller) {
        this.bookGrid = bookGrid;
        this.favGrid = favGrid;
        this.bookList = bookList;
        this.controller = controller;
    }

    public void setCurrentQuery(String query) {
        this.currentQuery = query;
    }

    public List<Book> getBookList() { return bookList; }

    public void refreshBookGrid() {
        if (bookGrid != null) fillGrid(bookGrid, false);
    }

    public void fillGrid(FlowPane grid, boolean favouriteOnly) {
        grid.getChildren().clear();
        int count = 0;
        for (Book book : bookList) {
            if (favouriteOnly && !book.isFavourite()) continue;
            if (!favouriteOnly && !matchesQuery(book, currentQuery)) continue;
            grid.getChildren().add(controller.createBookTile(book));
            count++;
        }

        if (count == 0) {
            VBox msg = new VBox(8);
            msg.setAlignment(Pos.CENTER);
            msg.setStyle("-fx-padding: 60;");
            Label icon = new Label(favouriteOnly ? "🤍" : "📚");
            icon.setStyle("-fx-font-size: 40px;");
            Label line = new Label(favouriteOnly
                    ? "No favourites yet."
                    : "No books found. Click '+ Add Book' to get started.");
            line.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
            line.setWrapText(true);
            msg.getChildren().addAll(icon, line);
            grid.getChildren().add(msg);
        }
    }

    public void filterBookGrid(String query) {
        if (bookGrid == null) return;
        this.currentQuery = query;
        this.bookList = Library.loadBooks();
        bookGrid.getChildren().clear();
        int shown = 0;
        for (Book book : bookList) {
            if (matchesQuery(book, query)) {
                bookGrid.getChildren().add(controller.createBookTile(book));
                shown++;
            }
        }
        if (shown == 0) showNoResults(bookGrid, query);
    }

    public void sortByTitle() {
        bookList.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        refreshBookGrid();
    }

    public void sortByDate() {
        bookList.sort((a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
        refreshBookGrid();
    }

    public void sortByProgress() {
        bookList.sort((a, b) -> Double.compare(b.getProgressValue(), a.getProgressValue()));
        refreshBookGrid();
    }

    private boolean matchesQuery(Book book, String query) {
        if (query == null || query.isEmpty()) return true;
        String title = book.getTitle().toLowerCase().replace("_", " ").replace("-", " ");
        String normalizedQuery = query.toLowerCase().replace("_", " ").replace("-", " ");
        for (String word : normalizedQuery.split("\\s+")) {
            if (!title.contains(word)) return false;
        }
        return true;
    }

    private void showNoResults(FlowPane grid, String query) {
        VBox msgBox = new VBox(8);
        msgBox.setAlignment(Pos.CENTER);
        msgBox.setStyle("-fx-padding: 60;");
        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 40px;");
        Label line1 = new Label(query.isEmpty() ? "No books yet." : "No results for \"" + query + "\"");
        line1.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label line2 = new Label(query.isEmpty() ? "Click '+ Add Book'." : "Try a different search.");
        line2.setStyle("-fx-text-fill: #777777; -fx-font-size: 12px;");
        msgBox.getChildren().addAll(icon, line1, line2);
        grid.getChildren().add(msgBox);
    }
}