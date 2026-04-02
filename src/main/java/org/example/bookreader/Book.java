package org.example.bookreader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String title;
    private String pathFile;
    private int totalPages;
    private int lastReadPageNumber;

    // renamed from isFavourite to favourite — Jackson was stripping "is" and
    // couldn't match the setter "setFavouriteStatus", so it always loaded as false
    @JsonProperty("favourite")
    private boolean favourite;

    private String category;
    private String coverPath;
    private double progressValue;
    private long dateAdded;

    public Book() {}

    public Book(String title, String path, int total, String category, double progress, String coverPath, int lastReadPageNumber) {
        this.title = title;
        this.pathFile = path;
        this.coverPath = coverPath;
        this.totalPages = total;
        this.lastReadPageNumber = 0;
        this.favourite = false;
        this.category = category;
        this.progressValue = progress;
        this.dateAdded = System.currentTimeMillis();
    }

    // Getters
    public String getTitle() { return title; }
    public String getFilePath() { return pathFile; }
    public int getTotalPages() { return totalPages; }
    public int getLastReadPageNumber() { return lastReadPageNumber; }

    @JsonProperty("favourite")
    public boolean isFavourite() { return favourite; }

    public String getCoverPath() { return coverPath; }
    public String getCategory() { return category; }
    public double getProgressValue() { return progressValue; }
    public long getDateAdded() { return dateAdded; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setFilePath(String path) { this.pathFile = path; }
    public void setTotalPages(int total) { this.totalPages = total; }
    public void setLastReadPageNumber(int last) { this.lastReadPageNumber = last; }

    // setter name matches the field so Jackson can call it during deserialization
    @JsonProperty("favourite")
    public void setFavourite(boolean fav) { this.favourite = fav; }


    public void setFavouriteStatus(boolean fav) { this.favourite = fav; }

    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
    public void setCategory(String category) { this.category = category; }
    public void setProgressValue(double p) { this.progressValue = p; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public static class NotebookHandler {
        private VBox notebookPanel;
        private TextArea notesTextArea;
        private boolean notebookPanelVisible = false;
        private Book currentBook;

        public NotebookHandler(VBox notebookPanel, TextArea notesTextArea) {
            this.notebookPanel = notebookPanel;
            this.notesTextArea = notesTextArea;
        }

        public void setCurrentBook(Book book) {
            this.currentBook = book;
        }

        public void toggleNotebookPanel() {
            if (notebookPanel == null) return;
            notebookPanelVisible = !notebookPanelVisible;
            if (notebookPanelVisible) {
                loadNotes();
                notebookPanel.setVisible(true);
                notebookPanel.setManaged(true);
            } else {
                notebookPanel.setVisible(false);
                notebookPanel.setManaged(false);
            }
        }

        private void loadNotes() {
            if (currentBook == null || notesTextArea == null) return;
            String path = notesFilePath();
            try {
                if (Files.exists(Paths.get(path))) {
                    notesTextArea.setText(new String(Files.readAllBytes(Paths.get(path))));
                } else {
                    notesTextArea.setText("");
                }
            } catch (IOException e) {
                System.out.println("Error loading notes: " + e.getMessage());
            }
        }

        public void saveNotes() {
            if (currentBook == null || notesTextArea == null) return;
            String path = notesFilePath();
            try {
                Files.createDirectories(Paths.get("notes"));
                Files.write(Paths.get(path), notesTextArea.getText().getBytes());
            } catch (IOException e) {
                System.out.println("Error saving notes: " + e.getMessage());
            }
        }

        private String notesFilePath() {
            return "notes/" + currentBook.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_notes.txt";
        }
    }
}