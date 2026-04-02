package org.example.bookreader;

import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NotebookHandler {
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