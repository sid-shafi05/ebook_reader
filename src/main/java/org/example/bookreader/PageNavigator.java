package org.example.bookreader;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;

public class PageNavigator {
    private int currentPage;
    private FileTypeManager fileTypeManager;
    private ImageView pdfView;
    private Label pageNumberLabel;
    private Label bookTitleLabel;
    private Slider pageSlider;
    private Label sliderMinLabel;
    private Label sliderMaxLabel;
    private Label sliderCurrentPageLabel;
    private ScrollPane pageScrollPane;
    private boolean sliderDragging = false;
    private double zoomLevel = 1.0;

    // Stored so zoomIn/zoomOut can update the label too
    private Button zoomResetBtn;

    // Prevents a slow render from overwriting a newer page request
    private volatile int renderRequestId = 0;

    private Runnable onPageChanged;

    public PageNavigator(ImageView pdfView, Label pageNumberLabel, Label bookTitleLabel,
                         Slider pageSlider, Label sliderMinLabel, Label sliderMaxLabel,
                         Label sliderCurrentPageLabel, ScrollPane pageScrollPane) {
        this.pdfView = pdfView;
        this.pageNumberLabel = pageNumberLabel;
        this.bookTitleLabel = bookTitleLabel;
        this.pageSlider = pageSlider;
        this.sliderMinLabel = sliderMinLabel;
        this.sliderMaxLabel = sliderMaxLabel;
        this.sliderCurrentPageLabel = sliderCurrentPageLabel;
        this.pageScrollPane = pageScrollPane;
    }

    public void setOnPageChanged(Runnable callback) {
        this.onPageChanged = callback;
    }

    /** Call this from BookController after building the navigator so zoom label updates work. */
    public void setZoomResetBtn(Button btn) {
        this.zoomResetBtn = btn;
    }

    public void init(Book book, int startPage, FileTypeManager fileTypeManager) {
        this.currentPage = startPage;
        this.fileTypeManager = fileTypeManager;

        int totalPages = fileTypeManager.getTotalPage();
        pageSlider.setMin(1);
        pageSlider.setMax(totalPages);
        pageSlider.setValue(currentPage + 1);
        sliderMinLabel.setText("1");
        sliderMaxLabel.setText(String.valueOf(totalPages));
        if (sliderCurrentPageLabel != null)
            sliderCurrentPageLabel.setText("Page " + (currentPage + 1));

        pageSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sliderCurrentPageLabel != null)
                sliderCurrentPageLabel.setText("Page " + newVal.intValue());
            updateSliderTrackFill();
        });

        updateSliderTrackFill();

        pageSlider.setOnMousePressed(e -> sliderDragging = true);
        pageSlider.setOnMouseReleased(e -> {
            sliderDragging = false;
            int newPage = (int) pageSlider.getValue() - 1;
            if (newPage != currentPage && newPage >= 0 && newPage < totalPages) {
                currentPage = newPage;
                renderCurrentPage(book);
                if (onPageChanged != null) onPageChanged.run();
            }
        });

        pdfView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null)
                pdfView.fitWidthProperty().bind(newScene.widthProperty().multiply(0.95));
        });
        if (pdfView.getScene() != null)
            pdfView.fitWidthProperty().bind(pdfView.getScene().widthProperty().multiply(0.95));
    }

    private void updateSliderTrackFill() {
        if (pageSlider == null) return;
        double pct = (pageSlider.getValue() - pageSlider.getMin())
                / (pageSlider.getMax() - pageSlider.getMin()) * 100.0;
        pageSlider.lookupAll(".track").forEach(node ->
                node.setStyle(
                        "-fx-background-color: linear-gradient(to right, " +
                                "#c8a96e " + pct + "%, " +
                                "#181828 " + pct + "%" +
                                "); -fx-background-radius: 3; -fx-pref-height: 4px;"
                )
        );
    }

    public void renderCurrentPage(Book book) {
        if (fileTypeManager == null) return;

        final int myRequestId = ++renderRequestId;

        pdfView.setImage(null);

        if (pageNumberLabel != null)
            pageNumberLabel.setText("Page " + (currentPage + 1) + " of " + fileTypeManager.getTotalPage());
        if (bookTitleLabel != null && book != null)
            bookTitleLabel.setText(book.getTitle());
        if (pageSlider != null && !sliderDragging)
            pageSlider.setValue(currentPage + 1);

        final int pageToRender = currentPage;

        Thread t = new Thread(() -> {
            Image img = fileTypeManager.getPage(pageToRender);
            Platform.runLater(() -> {
                if (myRequestId == renderRequestId) {
                    pdfView.setImage(img);
                    scrollToTop();
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    public void nextPage(Book book) {
        if (currentPage < fileTypeManager.getTotalPage() - 1) {
            currentPage++;
            renderCurrentPage(book);
            if (onPageChanged != null) onPageChanged.run();
        }
    }

    public void prevPage(Book book) {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage(book);
            if (onPageChanged != null) onPageChanged.run();
        }
    }

    public void zoomIn() {
        zoomLevel = Math.min(zoomLevel + 0.2, 3.0);
        applyZoom();
    }

    public void zoomOut() {
        zoomLevel = Math.max(zoomLevel - 0.2, 0.3);
        applyZoom();
    }

    public void zoomReset(Button btn) {
        // Accept the button here too in case it wasn't set via setZoomResetBtn
        if (btn != null) this.zoomResetBtn = btn;
        zoomLevel = 1.0;
        applyZoom();
    }

    private void applyZoom() {
        if (pdfView == null || pdfView.getScene() == null) return;
        pdfView.fitWidthProperty().unbind();
        pdfView.setFitWidth(pdfView.getScene().getWidth() * 0.95 * zoomLevel);
        if (zoomResetBtn != null)
            zoomResetBtn.setText((int)(zoomLevel * 100) + "%");
    }

    public void unbindWidth() {
        pdfView.fitWidthProperty().unbind();
    }

    private void scrollToTop() {
        if (pageScrollPane != null)
            Platform.runLater(() -> pageScrollPane.setVvalue(0));
    }

    public int getCurrentPage()          { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = page; }
}