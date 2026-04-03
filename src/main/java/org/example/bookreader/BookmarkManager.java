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

public class BookmarkManager {

    private static final String BOOKMARKS_FILE = "bookmarks.json";


    private static final ObjectMapper mapper = new ObjectMapper();


    public static void addBookmark(String bookPath, int pageNumber) {
        Bookmark newBookmark = new Bookmark(bookPath, pageNumber);
        addBookmark(newBookmark);
    }


    public static void addBookmark(Bookmark bookmark) {
        try {

            List<Bookmark> bookmarks = loadBookmarks();

            for (int i = 0; i < bookmarks.size(); i++) {

                Bookmark current = bookmarks.get(i);


                boolean samePath = current.getBookPath().equals(bookmark.getBookPath());

                boolean samePage = current.getPageNumber() == bookmark.getPageNumber();

                if (samePath && samePage) {
                    bookmarks.remove(i);
                    break;
                }
            }


            bookmarks.add(bookmark);

            saveBookmarks(bookmarks);

        } catch (Exception e) {
            System.err.println("Error adding bookmark: " + e.getMessage());
        }
    }


    public static void removeBookmark(String bookPath, int pageNumber) {
        try {
            List<Bookmark> bookmarks = loadBookmarks();

            for (int i = 0; i < bookmarks.size(); i++) {

                Bookmark current = bookmarks.get(i);

                boolean samePath = current.getBookPath().equals(bookPath);
                boolean samePage = current.getPageNumber() == pageNumber;


                if (samePath && samePage) {
                    bookmarks.remove(i);
                    break;
                }
            }

            saveBookmarks(bookmarks);

        } catch (Exception e) {
            System.err.println("Error removing bookmark: " + e.getMessage());
        }
    }



    public static List<Bookmark> getBookmarksForBook(String bookPath) {
        try {
            List<Bookmark> allBookmarks = loadBookmarks();

            List<Bookmark> result = new ArrayList<>();

            for (Bookmark b : allBookmarks) {
                if (b.getBookPath().equals(bookPath)) {
                    result.add(b);
                }
            }

            for (int i = 0; i < result.size() - 1; i++) {
                for (int j = i + 1; j < result.size(); j++) {
                    if (result.get(i).getPageNumber() > result.get(j).getPageNumber()) {
                        Bookmark temp = result.get(i);
                        result.set(i, result.get(j));
                        result.set(j, temp);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("Error getting bookmarks: " + e.getMessage());
            return new ArrayList<>(); // return empty list if something goes wrong
        }
    }



    public static boolean isBookmarked(String bookPath, int pageNumber) {
        List<Bookmark> bookmarks = getBookmarksForBook(bookPath);

        for (Bookmark b : bookmarks) {
            if (b.getPageNumber() == pageNumber) {
                return true;
            }
        }

        return false;
    }



    private static void saveBookmarks(List<Bookmark> bookmarks) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(BOOKMARKS_FILE), bookmarks);
        } catch (Exception e) {
            System.err.println("Error saving bookmarks: " + e.getMessage());
        }
    }



    public static List<Bookmark> loadBookmarks() {
        File file = new File(BOOKMARKS_FILE);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            List<Bookmark> bookmarks = mapper.readValue(file, new TypeReference<List<Bookmark>>() {});
            return bookmarks;

        } catch (Exception e) {
            System.err.println("Error loading bookmarks: " + e.getMessage());
            return new ArrayList<>();
        }
    }



    public static void clearBookmarksForBook(String bookPath) {
        try {
            List<Bookmark> allBookmarks = loadBookmarks();

            List<Bookmark> remaining = new ArrayList<>();

            for (Bookmark b : allBookmarks) {
                if (!b.getBookPath().equals(bookPath)) {
                    remaining.add(b);
                }
            }

            saveBookmarks(remaining);

        } catch (Exception e) {
            System.err.println("Error clearing bookmarks: " + e.getMessage());
        }
    }


    public static int showBookmarkDialog(String bookTitle, List<Bookmark> bookmarks) {

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Bookmarks");


        final int[] selectedPage = {-1};


        VBox root = new VBox(0);
        root.getStyleClass().add("bookmark-dialog-root");
        root.setPrefWidth(450);
        root.setMaxWidth(450);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("bookmark-dialog-header");
        header.setPadding(new Insets(22, 20, 22, 20));

        Label titleLabel = new Label("🔖 Bookmarks");
        titleLabel.getStyleClass().add("bookmark-dialog-title");
        header.getChildren().add(titleLabel);

        HBox subtitleBox = new HBox();
        subtitleBox.setAlignment(Pos.CENTER);
        subtitleBox.setPadding(new Insets(14, 20, 10, 20));

        Label bookLabel = new Label(bookTitle);
        bookLabel.getStyleClass().add("bookmark-dialog-subtitle");
        subtitleBox.getChildren().add(bookLabel);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 25, 25, 25));
        content.setAlignment(Pos.TOP_CENTER);

        if (bookmarks.isEmpty()) {

            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30, 20, 30, 20));
            emptyBox.getStyleClass().add("bookmark-empty-box");

            Label emptyIcon = new Label("📑");
            emptyIcon.setStyle("-fx-font-size: 48px;");

            Label emptyLabel = new Label("No Bookmarks Yet");
            emptyLabel.getStyleClass().add("bookmark-empty-title");

            Label emptyHint = new Label("Click the 🔖 button while reading\nto bookmark a page!");
            emptyHint.getStyleClass().add("bookmark-empty-hint");

            emptyBox.getChildren().addAll(emptyIcon, emptyLabel, emptyHint);
            content.getChildren().add(emptyBox);

        } else {

            Label instructionLabel = new Label("Select a page to jump to:");
            instructionLabel.getStyleClass().add("bookmark-instruction");

            ListView<String> listView = new ListView<>();
            listView.getStyleClass().add("bookmark-list");
            listView.setPrefHeight(200);

            for (Bookmark bookmark : bookmarks) {
                int displayPage = bookmark.getPageNumber() + 1; // +1 because pages start at 0 internally
                listView.getItems().add("📄 Page " + displayPage);
            }

            listView.setOnMouseClicked(event -> {
                boolean isDoubleClick = event.getClickCount() == 2;

                if (isDoubleClick) {
                    int index = listView.getSelectionModel().getSelectedIndex();
                    boolean somethingSelected = index >= 0;

                    if (somethingSelected) {
                        selectedPage[0] = bookmarks.get(index).getPageNumber();
                        dialog.close();
                    }
                }
            });

            content.getChildren().addAll(instructionLabel, listView);
        }

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 25, 25, 25));

        if (!bookmarks.isEmpty()) {
            Button okButton = new Button("Jump to Page");
            okButton.getStyleClass().add("bookmark-ok-btn");

            okButton.setOnAction(e -> {
                ListView<String> lv = (ListView<String>) content.getChildren().get(1);

                int index = lv.getSelectionModel().getSelectedIndex();
                boolean somethingSelected = index >= 0;

                if (somethingSelected) {
                    selectedPage[0] = bookmarks.get(index).getPageNumber();
                    dialog.close();
                }
            });

            buttonBox.getChildren().add(okButton);
        }

        String cancelButtonText = bookmarks.isEmpty() ? "Close" : "Cancel";
        Button cancelButton = new Button(cancelButtonText);
        cancelButton.getStyleClass().add("bookmark-cancel-btn");

        cancelButton.setOnAction(e -> {
            dialog.close();
        });

        buttonBox.getChildren().add(cancelButton);

        root.getChildren().addAll(header, subtitleBox, content, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        String cssPath = BookmarkManager.class
                .getResource("/org/example/bookreader/application.css")
                .toExternalForm();
        scene.getStylesheets().add(cssPath);


        dialog.setScene(scene);
        dialog.showAndWait();


        return selectedPage[0];
    }
}