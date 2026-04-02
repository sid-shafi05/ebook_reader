package org.example.bookreader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;

public class BookController {
    private long sessionStartTime;
    private int sessionStartPage;
    private Book currentBook;
    private int currentPage;
    private int highestPageReached;
    private boolean focusModeActive = false;
    private Timeline readingTimer;
    private int readingSeconds = 0;
    private ContextMenu currentMenu;

    @FXML private ScrollPane pageScrollPane;
    @FXML private ImageView pdfView;
    @FXML private Label pageNumberLabel;
    @FXML private Label bookTitleLabel;
    @FXML private Button bookmarkButton;
    @FXML private Slider pageSlider;
    @FXML private Label sliderMinLabel;
    @FXML private Label sliderMaxLabel;
    @FXML private Label sliderCurrentPageLabel;
    @FXML private Button focusModeButton;
    @FXML private Button menuButton;
    @FXML private Button closeNotebookButton;
    @FXML private HBox sliderSection;
    @FXML private HBox controlsSection;
    @FXML private VBox notebookPanel;
    @FXML private TextArea notesTextArea;

    // highlight fields
    @FXML private javafx.scene.canvas.Canvas highlightCanvas;
    private double dragStartX, dragStartY;
    private java.util.Map<Integer, java.util.List<double[]>> highlights = new java.util.HashMap<>();

    // chat fields
    @FXML private VBox chatPanel;
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;
    private List<java.util.Map<String, String>> chatHistory = new ArrayList<>();
    private static final String API_KEY = "AIzaSyCFuWvlFl81_584ErqzLESRjc6LXFl8r1M";

    private boolean sliderDragging = false;
    private ColorAdjust colorAdjust = new ColorAdjust();
    private boolean notebookPanelVisible = false;
    private boolean chatPanelVisible = false;

    private FileTypeManager fileTypeManager;

    public void startSession(Book book) {
        this.currentBook = book;
        this.currentPage = book.getLastReadPageNumber();
        this.sessionStartTime = System.currentTimeMillis();
        this.sessionStartPage = currentPage;
        this.highestPageReached = currentPage;
        try {
            fileTypeManager = new FileTypeManager();
            fileTypeManager.fileType(book.getFilePath());

            int totalPages = fileTypeManager.getTotalPage();
            pageSlider.setMin(1);
            pageSlider.setMax(totalPages);
            pageSlider.setValue(currentPage + 1);
            sliderMinLabel.setText("1");
            sliderMaxLabel.setText(String.valueOf(totalPages));

            if (sliderCurrentPageLabel != null) {
                sliderCurrentPageLabel.setText("Page " + (currentPage + 1));
            }

            pageSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int pageNum = newVal.intValue();
                if (sliderCurrentPageLabel != null) {
                    sliderCurrentPageLabel.setText("Page " + pageNum);
                }
            });

            pageSlider.setOnMousePressed(event -> sliderDragging = true);
            pageSlider.setOnMouseReleased(event -> {
                sliderDragging = false;
                int newPage = (int) pageSlider.getValue() - 1;
                if (newPage != currentPage && newPage >= 0 && newPage < totalPages) {
                    currentPage = newPage;
                    if (currentPage > highestPageReached) {
                        highestPageReached = currentPage;
                    }
                    renderCurrentPage();
                }
            });

            pdfView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    pdfView.fitWidthProperty().bind(newScene.widthProperty().multiply(0.95));
                }
            });
            if (pdfView.getScene() != null) {
                pdfView.fitWidthProperty().bind(pdfView.getScene().widthProperty().multiply(0.95));
            }
            renderCurrentPage();
            startReadingTimer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void renderCurrentPage() {
        if (fileTypeManager != null) {
            pdfView.setImage(null);
            System.gc();
            pdfView.setImage(fileTypeManager.getPage(currentPage));
            if (pageNumberLabel != null) {
                pageNumberLabel.setText("Page " + (currentPage + 1) + " of " + fileTypeManager.getTotalPage());
            }
            if (bookTitleLabel != null && currentBook != null) {
                bookTitleLabel.setText(currentBook.getTitle());
            }
            if (pageSlider != null && !sliderDragging) {
                pageSlider.setValue(currentPage + 1);
            }

            if (highlightCanvas != null) {
                javafx.scene.canvas.GraphicsContext gc = highlightCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, highlightCanvas.getWidth(), highlightCanvas.getHeight());
                java.util.List<double[]> pageHighlights = highlights.get(currentPage);
                if (pageHighlights != null) {
                    gc.setFill(javafx.scene.paint.Color.rgb(255, 255, 0, 0.35));
                    for (double[] r : pageHighlights) {
                        gc.fillRect(r[0], r[1], r[2], r[3]);
                    }
                }
            }

            updateBookmarkButtonStyle();
            scrollToTop();
        }
    }

    private void scrollToTop() {
        if (pageScrollPane != null) {
            javafx.application.Platform.runLater(() -> pageScrollPane.setVvalue(0));
        }
    }

    @FXML
    public void nextButtonLogic() {
        if (currentPage < fileTypeManager.getTotalPage() - 1) {
            currentPage++;
            if (currentPage > highestPageReached) {
                highestPageReached = currentPage;
            }
            renderCurrentPage();
        }
    }

    @FXML
    public void prevButtonLogic() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
        }
    }

    @FXML
    public void onBackButtonClick() {
        stopSession();
        javafx.stage.Stage stage = (javafx.stage.Stage) pdfView.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void toggleBookmark() {
        if (currentBook == null) return;
        if (BookmarkManager.isBookmarked(currentBook.getFilePath(), currentPage)) {
            BookmarkManager.removeBookmark(currentBook.getFilePath(), currentPage);
        } else {
            BookmarkManager.addBookmark(currentBook.getFilePath(), currentPage);
        }
        updateBookmarkButtonStyle();
    }

    private void updateBookmarkButtonStyle() {
        if (bookmarkButton == null || currentBook == null) return;
        if (BookmarkManager.isBookmarked(currentBook.getFilePath(), currentPage)) {
            bookmarkButton.setText("🔖");
            bookmarkButton.setStyle("-fx-font-size: 20; -fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        } else {
            bookmarkButton.setText("🔖");
            bookmarkButton.setStyle("");
            bookmarkButton.getStyleClass().clear();
            bookmarkButton.getStyleClass().add("bookmark-button");
        }
    }

    @FXML
    public void showBookmarksPanel() {
        if (currentBook == null) return;
        List<Bookmark> bookmarks = BookmarkManager.getBookmarksForBook(currentBook.getFilePath());
        int selectedPage = BookmarkManager.showBookmarkDialog(currentBook.getTitle(), bookmarks);
        if (selectedPage >= 0) {
            currentPage = selectedPage;
            if (currentPage > highestPageReached) {
                highestPageReached = currentPage;
            }
            renderCurrentPage();
        }
    }

    public boolean isCurrentPageBookmarked() {
        if (currentBook == null) return false;
        return BookmarkManager.isBookmarked(currentBook.getFilePath(), currentPage);
    }

    public List<Bookmark> getCurrentBookBookmarks() {
        if (currentBook == null) return new ArrayList<>();
        return BookmarkManager.getBookmarksForBook(currentBook.getFilePath());
    }

    public void stopSession() {
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - sessionStartTime) / 1000;
        int pagesReadThisSession = highestPageReached - sessionStartPage;

        int totalPages = fileTypeManager.getTotalPage();
        double newProgress;
        if (highestPageReached >= totalPages - 1) {
            newProgress = 1.0;
        } else {
            newProgress = (double) highestPageReached / (totalPages - 1);
        }

        SingleReadingEvent event = new SingleReadingEvent(
                java.time.LocalDate.now().toString(),
                currentBook.getTitle(),
                pagesReadThisSession,
                seconds,
                currentBook.getCategory()
        );
        StatsManagement.saveNewEvent(event);

        currentBook.setLastReadPageNumber(currentPage);
        currentBook.setProgressValue(newProgress);

        List<Book> library = Library.loadBooks();
        for (Book b : library) {
            if (b.getFilePath().equals(currentBook.getFilePath())) {
                b.setLastReadPageNumber(currentPage);
                b.setProgressValue(newProgress);
            }
        }
        Library.saveBookList(library);

        stopReadingTimer();
        fileTypeManager.close();
        pdfView.fitWidthProperty().unbind();
    }

    // ===== HIGHLIGHT =====
    @FXML
    public void enableHighlight() {
        if (highlightCanvas == null) return;
        javafx.scene.canvas.GraphicsContext gc = highlightCanvas.getGraphicsContext2D();
        highlightCanvas.setCursor(javafx.scene.Cursor.CROSSHAIR);

        highlightCanvas.setOnMousePressed(e -> {
            dragStartX = e.getX();
            dragStartY = e.getY();
        });

        highlightCanvas.setOnMouseDragged(e -> {
            gc.clearRect(0, 0, highlightCanvas.getWidth(), highlightCanvas.getHeight());
            java.util.List<double[]> saved = highlights.get(currentPage);
            if (saved != null) {
                gc.setFill(javafx.scene.paint.Color.rgb(255, 255, 0, 0.35));
                for (double[] r : saved) gc.fillRect(r[0], r[1], r[2], r[3]);
            }
            double x = Math.min(dragStartX, e.getX());
            double y = Math.min(dragStartY, e.getY());
            double w = Math.abs(e.getX() - dragStartX);
            double h = Math.abs(e.getY() - dragStartY);
            gc.setFill(javafx.scene.paint.Color.rgb(255, 255, 0, 0.35));
            gc.fillRect(x, y, w, h);
        });

        highlightCanvas.setOnMouseReleased(e -> {
            double x = Math.min(dragStartX, e.getX());
            double y = Math.min(dragStartY, e.getY());
            double w = Math.abs(e.getX() - dragStartX);
            double h = Math.abs(e.getY() - dragStartY);
            if (w > 5 && h > 5) {
                highlights.computeIfAbsent(currentPage, k -> new java.util.ArrayList<>())
                        .add(new double[]{x, y, w, h});
            }
        });
    }

    // ===== AI CHAT =====
    @FXML
    public void toggleChatPanel() {
        chatPanelVisible = !chatPanelVisible;
        chatPanel.setVisible(chatPanelVisible);
        chatPanel.setManaged(chatPanelVisible);
    }

    @FXML
    public void sendChatMessage() {
        if (chatInput == null) return;
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) return;

        addChatBubble("You", userMessage, "#0f3460", "#4fc3f7");
        chatInput.clear();

        // 1. Setup the context
        String bookContext = "You are a helpful reading assistant. The user is reading '" +
                (currentBook != null ? currentBook.getTitle() : "a book") +
                "' and is currently on page " + (currentPage + 1) +
                ". Answer their questions helpfully and concisely.";

        // Add user message to history
        java.util.Map<String, String> userMsg = new java.util.HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        chatHistory.add(userMsg);

        Label loading = new Label("AI is thinking...");
        loading.setStyle("-fx-text-fill: #666; -fx-font-size: 10; -fx-padding: 4;");
        chatMessages.getChildren().add(loading);

        new Thread(() -> {
            // 2. PASS BOTH: Send the context AND the history
            // If your callAPI only takes one String, use: callAPI(bookContext + "\n\nUser says: " + userMessage)
            String reply = callAPI(bookContext, userMessage);

            javafx.application.Platform.runLater(() -> {
                chatMessages.getChildren().remove(loading);
                addChatBubble("AI", reply, "#1a1a2e", "#e0e0e0");

                java.util.Map<String, String> assistantMsg = new java.util.HashMap<>();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", reply);
                chatHistory.add(assistantMsg);
            });
        }).start();
    }
    private void addChatBubble(String sender, String text, String bgColor, String textColor) {
        VBox bubble = new VBox(3);
        bubble.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-padding: 8 12;");
        bubble.setMaxWidth(240);

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(220);
        messageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + textColor + ";");

        bubble.getChildren().addAll(senderLabel, messageLabel);
        chatMessages.getChildren().add(bubble);
    }

    private String callAPI(String context, String userPrompt) {
        try {
            // 1. Ensure the URL is v1 (stable 2026)
            String url = "https://generativelanguage.googleapis.com/v1/gemini-1.5-flash:generateContent?key=" + API_KEY;

            // 2. Prepare the combined prompt
            String fullPrompt = context + "\n\nUser Question: " + userPrompt;
            String escapedPrompt = fullPrompt.replace("\"", "\\\"").replace("\n", "\\n");

            String jsonBody = "{" +
                    "\"contents\": [{\"parts\":[{\"text\": \"" + escapedPrompt + "\"}]}], " +
                    "\"safetySettings\": [" +
                    "{\"category\": \"HARM_CATEGORY_HARASSMENT\", \"threshold\": \"BLOCK_NONE\"}," +
                    "{\"category\": \"HARM_CATEGORY_HATE_SPEECH\", \"threshold\": \"BLOCK_NONE\"}," +
                    "{\"category\": \"HARM_CATEGORY_SEXUALLY_EXPLICIT\", \"threshold\": \"BLOCK_NONE\"}," +
                    "{\"category\": \"HARM_CATEGORY_DANGEROUS_CONTENT\", \"threshold\": \"BLOCK_NONE\"}" +
                    "]" +
                    "}";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            // 3. Handle Errors from the API side
            if (root.has("error")) {
                return "API Error: " + root.path("error").path("message").asText();
            }

            // 4. THE FIX: Precise path navigation for Gemini 1.5
            // Path: candidates -> [0] -> content -> parts -> [0] -> text
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode parts = firstCandidate.path("content").path("parts");

                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }

            // Helpful debugging if it still fails
            System.err.println("Unexpected JSON: " + response.body());
            return "AI Error: The API responded but the message content was hidden.";

        } catch (Exception e) {
            return "System Error: " + e.getMessage();
        }
    }

    // ===== FOCUS MODE =====
    @FXML
    public void toggleFocusMode() {
        focusModeActive = !focusModeActive;
        if (focusModeActive) {
            if (sliderSection != null) sliderSection.setVisible(false);
            if (controlsSection != null) controlsSection.setVisible(false);
            if (focusModeButton != null) focusModeButton.setStyle("-fx-background-color: #4f9eff; -fx-text-fill: white;");
        } else {
            if (sliderSection != null) sliderSection.setVisible(true);
            if (controlsSection != null) controlsSection.setVisible(true);
            if (focusModeButton != null) focusModeButton.setStyle("");
        }
    }

    // ===== COLOR FILTERS =====
    @FXML
    public void setColorFilterNormal() {
        pdfView.setEffect(null);
    }

    @FXML
    public void setColorFilterDark() {
        colorAdjust.setHue(0);
        colorAdjust.setBrightness(-0.5);
        colorAdjust.setContrast(0.6);
        colorAdjust.setSaturation(-0.3);
        pdfView.setEffect(colorAdjust);
    }

    @FXML
    public void setColorFilterSepia() {
        ColorAdjust sepiaAdjust = new ColorAdjust();
        sepiaAdjust.setHue(-0.1);
        sepiaAdjust.setBrightness(0.08);
        sepiaAdjust.setContrast(0.2);
        sepiaAdjust.setSaturation(-0.9);
        ColorInput warmOverlay = new ColorInput(0, 0, 2000, 2000, Color.web("#f5deb3"));
        Blend blendEffect = new Blend(BlendMode.MULTIPLY, sepiaAdjust, warmOverlay);
        pdfView.setEffect(blendEffect);
    }

    @FXML
    public void showColorFilterMenu() {
        if (currentMenu != null && currentMenu.isShowing()) {
            currentMenu.hide();
            currentMenu = null;
            return;
        }

        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-font-size: 13px; -fx-background-color: #2b2b2b; -fx-border-color: #3a3a3a;");

        MenuItem colorModesItem = new MenuItem("Color Modes");
        ContextMenu colorMenu = new ContextMenu();

        MenuItem normalMode = new MenuItem("Normal Mode");
        normalMode.setOnAction(e -> setColorFilterNormal());
        MenuItem darkMode = new MenuItem("Dark Mode");
        darkMode.setOnAction(e -> setColorFilterDark());
        MenuItem sepiaFilter = new MenuItem("Reading Mode");
        sepiaFilter.setOnAction(e -> setColorFilterSepia());
        colorMenu.getItems().addAll(normalMode, darkMode, sepiaFilter);

        colorModesItem.setOnAction(e -> {
            Bounds bounds = menuButton.localToScreen(menuButton.getBoundsInLocal());
            colorMenu.show(menuButton.getScene().getWindow(), bounds.getCenterX() + 100, bounds.getCenterY());
        });

        SeparatorMenuItem sep = new SeparatorMenuItem();

        MenuItem notebookItem = new MenuItem("Notebook");
        notebookItem.setOnAction(e -> {
            menu.hide();
            currentMenu = null;
            toggleNotebookPanel();
        });

        menu.getItems().addAll(colorModesItem, sep, notebookItem);

        if (menuButton != null) {
            Bounds bounds = menuButton.localToScreen(menuButton.getBoundsInLocal());
            menu.show(menuButton, bounds.getCenterX(), bounds.getCenterY() + 20);
            currentMenu = menu;
        }
    }

    // ===== NOTEBOOK =====
    @FXML
    public void toggleNotebookPanel() {
        if (notebookPanel == null) return;
        notebookPanelVisible = !notebookPanelVisible;
        if (notebookPanelVisible) {
            loadNotesFromFile();
            notebookPanel.setVisible(true);
            notebookPanel.setManaged(true);
        } else {
            notebookPanel.setVisible(false);
            notebookPanel.setManaged(false);
        }
    }

    private void loadNotesFromFile() {
        if (currentBook == null || notesTextArea == null) return;
        String notesFileName = "notes/" + currentBook.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_notes.txt";
        try {
            if (java.nio.file.Files.exists(java.nio.file.Paths.get(notesFileName))) {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(notesFileName)));
                notesTextArea.setText(content);
            } else {
                notesTextArea.setText("");
            }
        } catch (IOException e) {
            System.out.println("Error loading notes: " + e.getMessage());
        }
    }

    @FXML
    public void saveNotesPanel() {
        if (currentBook == null || notesTextArea == null) return;
        String notes = notesTextArea.getText();
        String notesFileName = "notes/" + currentBook.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_notes.txt";
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("notes"));
            java.nio.file.Files.write(java.nio.file.Paths.get(notesFileName), notes.getBytes());
        } catch (IOException e) {
            System.out.println("Error saving notes: " + e.getMessage());
        }
    }

    // ===== READING TIMER =====
    private void startReadingTimer() {
        readingSeconds = 0;
        readingTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> readingSeconds++));
        readingTimer.setCycleCount(Timeline.INDEFINITE);
        readingTimer.play();
    }

    private void stopReadingTimer() {
        if (readingTimer != null) readingTimer.stop();
    }
}
