package org.example.bookreader;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class BookAdder {
    private final String baseDataPath;
    private final List<Book> bookList;
    private final Runnable onBookAdded;

    public BookAdder(String baseDataPath, List<Book> bookList, Runnable onBookAdded) {
        this.baseDataPath = baseDataPath;
        this.bookList = bookList;
        this.onBookAdded = onBookAdded;
    }

    public void addBook() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a book");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported Files (PDF, CBZ)", "*.pdf", "*.cbz"),
                new FileChooser.ExtensionFilter("PDF Books", "*.pdf"),
                new FileChooser.ExtensionFilter("CBZ Comics", "*.cbz")
        );

        File selectedFile = chooser.showOpenDialog(null);
        if (selectedFile == null) return;

        String fileName = selectedFile.getName();
        boolean isComic = fileName.toLowerCase().endsWith(".cbz");
        String bookTitle = fileName.replaceAll("(?i)\\.(pdf|cbz)$", "");

        List<String> genres = List.of(
                "Textbook", "Mathematics", "Science", "Physics", "Chemistry",
                "Engineering", "Programming", "Economics", "Philosophy", "Literature",
                "Novel", "Fiction", "Science Fiction", "Fantasy", "Adventure",
                "Action", "Mystery", "Thriller", "Horror", "Romance", "Psychological",
                "Historical Fiction", "Period Drama", "Dystopian", "Supernatural",
                "History", "Biography", "Autobiography", "Self-Help", "Psychology",
                "Politics", "Travel", "True Crime", "Science & Nature", "Religion",
                "Comic / Manga", "Graphic Novel", "Superhero", "Other"
        );

        ChoiceDialog<String> genreDialog = new ChoiceDialog<>("Textbook", genres);
        genreDialog.setTitle("Add to Library");
        genreDialog.setHeaderText("Select a genre for: " + bookTitle);
        genreDialog.setContentText("Genre:");
        genreDialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/org/example/bookreader/application.css").toExternalForm()
        );
        genreDialog.getDialogPane().getStyleClass().add("choice-dialog");

        Optional<String> genreResult = genreDialog.showAndWait();
        if (genreResult.isEmpty()) return;
        String finalCategory = genreResult.get();

        //Adding....
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Adding Book");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Adding \"" + bookTitle + "\" to your library...");
        loadingAlert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        loadingAlert.show();

        // Do all heavy work (file copy, PDF open, cover render, JSON save) on a background thread
        Thread t = new Thread(() -> {
            try {
                File dir = new File(baseDataPath + File.separator + "booksdata");
                if (!dir.exists()) dir.mkdirs();

                File destination = new File(dir, fileName);
                java.nio.file.Files.copy(selectedFile.toPath(),
                        destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

                Image coverImage;
                int totalPages;
                if (isComic) {
                    ComicEngine comic = new ComicEngine(destination.getAbsolutePath());
                    totalPages = comic.getTotalPages();
                    coverImage = comic.getPage(0);
                } else {
                    PDFEngine pdf = new PDFEngine(destination.getAbsolutePath());
                    totalPages = pdf.getPageCount();
                    coverImage = pdf.renderingPage(0);
                    pdf.close();
                }

                String coverPath = saveCover(coverImage, bookTitle);
                Book newBook = new Book(bookTitle, destination.getAbsolutePath(),
                        totalPages, finalCategory, 0.0, coverPath, 0);

                Platform.runLater(() -> {
                    loadingAlert.close();
                    bookList.add(newBook);
                    Library.saveBookList(bookList);
                    if (onBookAdded != null) onBookAdded.run();
                });

            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingAlert.close();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to add book");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private String saveCover(Image image, String title) {
        String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
        String path = baseDataPath + File.separator + "covers"
                + File.separator + safeTitle + ".png";
        File file = new File(path);
        file.getParentFile().mkdirs();
        try {
            java.awt.image.BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            javax.imageio.ImageIO.write(bImage, "png", file);
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}