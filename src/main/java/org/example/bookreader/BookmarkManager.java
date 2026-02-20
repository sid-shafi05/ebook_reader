package org.example.bookreader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for bookmark operations. Persists to JSON file.
 */
public class BookmarkManager {
    private static final String BOOKMARKS_FILE = "bookmarks.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void addBookmark(String bookPath, int pageNumber) {
        addBookmark(new Bookmark(bookPath, pageNumber));
    }

    public static void addBookmark(Bookmark bookmark) {
        try {
            List<Bookmark> bookmarks = loadBookmarks();
            bookmarks.removeIf(b -> b.getBookPath().equals(bookmark.getBookPath()) &&
                                     b.getPageNumber() == bookmark.getPageNumber());
            bookmarks.add(bookmark);
            saveBookmarks(bookmarks);
        } catch (Exception e) {
            System.err.println("Error adding bookmark: " + e.getMessage());
        }
    }

    public static void removeBookmark(String bookPath, int pageNumber) {
        try {
            List<Bookmark> bookmarks = loadBookmarks();
            bookmarks.removeIf(b -> b.getBookPath().equals(bookPath) &&
                                    b.getPageNumber() == pageNumber);
            saveBookmarks(bookmarks);
        } catch (Exception e) {
            System.err.println("Error removing bookmark: " + e.getMessage());
        }
    }

    public static List<Bookmark> getBookmarksForBook(String bookPath) {
        try {
            List<Bookmark> allBookmarks = loadBookmarks();
            return allBookmarks.stream()
                    .filter(b -> b.getBookPath().equals(bookPath))
                    .sorted((b1, b2) -> Integer.compare(b1.getPageNumber(), b2.getPageNumber()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting bookmarks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static boolean isBookmarked(String bookPath, int pageNumber) {
        List<Bookmark> bookmarks = getBookmarksForBook(bookPath);
        return bookmarks.stream().anyMatch(b -> b.getPageNumber() == pageNumber);
    }

    public static Bookmark getBookmark(String bookPath, int pageNumber) {
        List<Bookmark> bookmarks = getBookmarksForBook(bookPath);
        return bookmarks.stream()
                .filter(b -> b.getPageNumber() == pageNumber)
                .findFirst().orElse(null);
    }

    private static void saveBookmarks(List<Bookmark> bookmarks) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(BOOKMARKS_FILE), bookmarks);
        } catch (Exception e) {
            System.err.println("Error saving bookmarks: " + e.getMessage());
        }
    }

    public static List<Bookmark> loadBookmarks() {
        File file = new File(BOOKMARKS_FILE);
        if (!file.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(file, new TypeReference<List<Bookmark>>() {});
        } catch (Exception e) {
            System.err.println("Error loading bookmarks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void clearBookmarksForBook(String bookPath) {
        try {
            List<Bookmark> bookmarks = loadBookmarks();
            bookmarks.removeIf(b -> b.getBookPath().equals(bookPath));
            saveBookmarks(bookmarks);
        } catch (Exception e) {
            System.err.println("Error clearing bookmarks: " + e.getMessage());
        }
    }

    /**
     * Show modern bookmark dialog and return selected page number.
     * Returns -1 if no page selected.
     */
    public static int showBookmarkDialog(String bookTitle, List<Bookmark> bookmarks) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Bookmarks");

        final int[] selectedPage = {-1};

        // Main container
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        root.setPrefWidth(450);
        root.setMaxWidth(450);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to right, #9C27B0, #673AB7); -fx-background-radius: 15 15 0 0;");
        header.setPadding(new Insets(25, 20, 25, 20));

        Label titleLabel = new Label("🔖 Bookmarks");
        titleLabel.setFont(Font.font("Segoe UI", 26));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-weight: bold;");
        header.getChildren().add(titleLabel);

        // Subtitle
        HBox subtitleBox = new HBox();
        subtitleBox.setAlignment(Pos.CENTER);
        subtitleBox.setPadding(new Insets(15, 20, 10, 20));

        Label bookLabel = new Label(bookTitle);
        bookLabel.setFont(Font.font("Segoe UI", 14));
        bookLabel.setTextFill(Color.rgb(100, 100, 100));
        subtitleBox.getChildren().add(bookLabel);

        // Content area
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 25, 25, 25));
        content.setAlignment(Pos.TOP_CENTER);

        if (bookmarks.isEmpty()) {
            // No bookmarks message
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30, 20, 30, 20));
            emptyBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

            Label emptyIcon = new Label("📑");
            emptyIcon.setFont(Font.font(48));

            Label emptyLabel = new Label("No Bookmarks Yet");
            emptyLabel.setFont(Font.font("Segoe UI", 16));
            emptyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");

            Label emptyHint = new Label("Click the 🔖 button while reading\nto bookmark a page!");
            emptyHint.setFont(Font.font("Segoe UI", 12));
            emptyHint.setTextFill(Color.rgb(120, 120, 120));

            emptyBox.getChildren().addAll(emptyIcon, emptyLabel, emptyHint);
            content.getChildren().add(emptyBox);
        } else {
            // Bookmarks list
            Label instructionLabel = new Label("Select a page to jump to:");
            instructionLabel.setFont(Font.font("Segoe UI", 13));
            instructionLabel.setTextFill(Color.rgb(80, 80, 80));

            ListView<String> listView = new ListView<>();
            listView.setPrefHeight(200);
            listView.setStyle("-fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");

            for (Bookmark bookmark : bookmarks) {
                listView.getItems().add("📄 Page " + (bookmark.getPageNumber() + 1));
            }

            // Double-click to select
            listView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    int index = listView.getSelectionModel().getSelectedIndex();
                    if (index >= 0) {
                        selectedPage[0] = bookmarks.get(index).getPageNumber();
                        dialog.close();
                    }
                }
            });

            content.getChildren().addAll(instructionLabel, listView);
        }

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 25, 25, 25));

        if (!bookmarks.isEmpty()) {
            Button okButton = new Button("Jump to Page");
            okButton.setFont(Font.font("Segoe UI", 13));
            okButton.setPrefWidth(140);
            okButton.setPrefHeight(40);
            okButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
            okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: #7B1FA2; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;"));
            okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;"));

            okButton.setOnAction(e -> {
                ListView<String> lv = (ListView<String>) content.getChildren().get(1);
                int index = lv.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    selectedPage[0] = bookmarks.get(index).getPageNumber();
                    dialog.close();
                }
            });

            buttonBox.getChildren().add(okButton);
        }

        Button cancelButton = new Button(bookmarks.isEmpty() ? "Close" : "Cancel");
        cancelButton.setFont(Font.font("Segoe UI", 13));
        cancelButton.setPrefWidth(140);
        cancelButton.setPrefHeight(40);
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #666666; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #666666; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 8;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #666666; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;"));
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().add(cancelButton);

        // Assemble everything
        root.getChildren().addAll(header, subtitleBox, content, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        dialog.showAndWait();
        return selectedPage[0];
    }
}
