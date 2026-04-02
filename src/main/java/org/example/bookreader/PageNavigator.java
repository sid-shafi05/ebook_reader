package org.example.bookreader;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

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

    public void init(Book book, int startPage, FileTypeManager fileTypeManager) {
        this.currentPage = startPage;
        this.fileTypeManager = fileTypeManager;

        int totalPages = fileTypeManager.getTotalPage();
        pageSlider.setMin(1);
        pageSlider.setMax(totalPages);
        pageSlider.setValue(currentPage + 1);
        sliderMinLabel.setText("1");
        sliderMaxLabel.setText(String.valueOf(totalPages));
        if (sliderCurrentPageLabel != null) {
            sliderCurrentPageLabel.setText("Page " + (currentPage + 1));
        }

        // FIX 3: update slider track fill whenever value changes
        pageSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sliderCurrentPageLabel != null) {
                sliderCurrentPageLabel.setText("Page " + newVal.intValue());
            }
            updateSliderTrackFill();
        });

        // Initial fill paint
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
            if (newScene != null) {
                pdfView.fitWidthProperty().bind(newScene.widthProperty().multiply(0.95));
            }
        });
        if (pdfView.getScene() != null) {
            pdfView.fitWidthProperty().bind(pdfView.getScene().widthProperty().multiply(0.95));
        }
    }

    /**
     * FIX 3: Paints the slider track so the portion left of the thumb is gold
     * and the right portion is dark. JavaFX CSS alone cannot do this dynamically,
     * so we apply a two-color background via inline style on the track node.
     */
    private void updateSliderTrackFill() {
        if (pageSlider == null) return;
        double pct = (pageSlider.getValue() - pageSlider.getMin())
                / (pageSlider.getMax() - pageSlider.getMin()) * 100.0;

        // Find the .track node inside the slider and style it
        pageSlider.lookupAll(".track").forEach(node -> {
            node.setStyle(
                    "-fx-background-color: linear-gradient(to right, " +
                            "#c8a96e " + pct + "%, " +
                            "#181828 " + pct + "%" +
                            "); " +
                            "-fx-background-radius: 3; " +
                            "-fx-pref-height: 4px;"
            );
        });
    }

    public void renderCurrentPage(Book book) {
        if (fileTypeManager == null) return;
        pdfView.setImage(null);
        System.gc();
        pdfView.setImage(fileTypeManager.getPage(currentPage));

        if (pageNumberLabel != null) {
            pageNumberLabel.setText("Page " + (currentPage + 1) + " of " + fileTypeManager.getTotalPage());
        }
        if (bookTitleLabel != null && book != null) {
            bookTitleLabel.setText(book.getTitle());
        }
        if (pageSlider != null && !sliderDragging) {
            pageSlider.setValue(currentPage + 1);
        }
        scrollToTop();
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
        applyZoom(null);
    }

    public void zoomOut() {
        zoomLevel = Math.max(zoomLevel - 0.2, 0.3);
        applyZoom(null);
    }

    public void zoomReset(javafx.scene.control.Button zoomResetBtn) {
        zoomLevel = 1.0;
        applyZoom(zoomResetBtn);
    }

    public void applyZoom(javafx.scene.control.Button zoomResetBtn) {
        if (pdfView == null || pdfView.getScene() == null) return;
        pdfView.fitWidthProperty().unbind();
        pdfView.setFitWidth(pdfView.getScene().getWidth() * 0.95 * zoomLevel);
        if (zoomResetBtn != null) {
            zoomResetBtn.setText((int)(zoomLevel * 100) + "%");
        }
    }

    public void unbindWidth() {
        pdfView.fitWidthProperty().unbind();
    }

    private void scrollToTop() {
        if (pageScrollPane != null) {
            javafx.application.Platform.runLater(() -> pageScrollPane.setVvalue(0));
        }
    }

    public int getCurrentPage()          { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = page; }
}