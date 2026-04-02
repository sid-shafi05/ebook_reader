package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class BookController {

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
    @FXML private HBox sliderSection;
    @FXML private HBox controlsSection;
    @FXML private VBox notebookPanel;
    @FXML private TextArea notesTextArea;
    @FXML private Canvas highlightCanvas;
    @FXML private VBox chatPanel;
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;
    @FXML private Button zoomResetBtn;

    private FileTypeManager fileTypeManager;

    // Handlers
    private SessionManager sessionManager;
    private PageNavigator navigator;
    private BookmarkHandler bookmarkHandler;
    private HighlightHandler highlightHandler;
    private ChatHandler chatHandler;
    private ReaderMenuHandler menuHandler;
    private NotebookHandler notebookHandler;

    public void startSession(Book book) {
        try {
            fileTypeManager = new FileTypeManager();
            fileTypeManager.fileType(book.getFilePath());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int startPage = book.getLastReadPageNumber();

        // Init all handlers
        navigator = new PageNavigator(pdfView, pageNumberLabel, bookTitleLabel,
                pageSlider, sliderMinLabel, sliderMaxLabel,
                sliderCurrentPageLabel, pageScrollPane);
        navigator.setOnPageChanged(() -> afterPageChange(book));
        navigator.init(book, startPage, fileTypeManager);

        sessionManager = new SessionManager();
        sessionManager.startSession(book, startPage);

        bookmarkHandler = new BookmarkHandler(bookmarkButton, navigator);
        bookmarkHandler.setCurrentBook(book);

        highlightHandler = new HighlightHandler(highlightCanvas);
        highlightHandler.setCurrentPage(startPage);

        chatHandler = new ChatHandler(chatPanel, chatMessages, chatInput, navigator);
        chatHandler.setCurrentBook(book);

        menuHandler = new ReaderMenuHandler(pdfView, focusModeButton, menuButton,
                sliderSection, controlsSection);
        menuHandler.setOnNotebookToggle(() -> notebookHandler.toggleNotebookPanel());

        notebookHandler = new NotebookHandler(notebookPanel, notesTextArea);
        notebookHandler.setCurrentBook(book);

        // First render
        navigator.renderCurrentPage(book);
        bookmarkHandler.updateBookmarkButtonStyle();
        highlightHandler.drawHighlightsForPage(startPage);
    }

    // Called after every page change to sync dependent handlers
    private void afterPageChange(Book book) {
        int page = navigator.getCurrentPage();
        sessionManager.updateHighestPage(page);
        bookmarkHandler.updateBookmarkButtonStyle();
        highlightHandler.setCurrentPage(page);
        highlightHandler.drawHighlightsForPage(page);
    }

    @FXML public void nextButtonLogic() {
        navigator.nextPage(sessionManager.getCurrentBook());
    }

    @FXML public void prevButtonLogic() {
        navigator.prevPage(sessionManager.getCurrentBook());
    }

    @FXML public void onBackButtonClick() {
        sessionManager.stopSession(
                navigator.getCurrentPage(),
                fileTypeManager.getTotalPage(),
                fileTypeManager
        );
        navigator.unbindWidth();
        fileTypeManager.close();
        javafx.stage.Stage stage = (javafx.stage.Stage) pdfView.getScene().getWindow();
        stage.close();
    }

    @FXML public void toggleBookmark() {
        bookmarkHandler.toggleBookmark();
    }

    @FXML public void showBookmarksPanel() {
        bookmarkHandler.showBookmarksPanel();
        navigator.renderCurrentPage(sessionManager.getCurrentBook());
        afterPageChange(sessionManager.getCurrentBook());
    }

    @FXML public void enableHighlight() {
        highlightHandler.enableHighlight();
    }

    @FXML public void toggleChatPanel() {
        chatHandler.toggleChatPanel();
    }

    @FXML public void sendChatMessage() {
        chatHandler.sendMessage();
    }

    @FXML public void toggleFocusMode() {
        menuHandler.toggleFocusMode();
    }

    @FXML public void showColorFilterMenu() {
        menuHandler.showColorFilterMenu();
    }

    @FXML public void setColorFilterNormal() { menuHandler.setColorFilterNormal(); }
    @FXML public void setColorFilterDark()   { menuHandler.setColorFilterDark(); }
    @FXML public void setColorFilterSepia()  { menuHandler.setColorFilterSepia(); }

    @FXML public void toggleNotebookPanel() {
        notebookHandler.toggleNotebookPanel();
    }

    @FXML public void saveNotesPanel() {
        notebookHandler.saveNotes();
    }

    @FXML public void zoomIn()    { navigator.zoomIn(); }
    @FXML public void zoomOut()   { navigator.zoomOut(); }
    @FXML public void zoomReset() { navigator.zoomReset(zoomResetBtn); }
}