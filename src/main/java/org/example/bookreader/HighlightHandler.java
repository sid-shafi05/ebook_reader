package org.example.bookreader;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighlightHandler {
    private Canvas highlightCanvas;
    private ImageView pdfView;
    private Map<Integer, List<double[]>> highlights = new HashMap<>();
    private double dragStartX, dragStartY;
    private int currentPage = 0;

    public HighlightHandler(Canvas highlightCanvas) {
        this.highlightCanvas = highlightCanvas;
    }

    /**
     * Pass the ImageView so the canvas can stay in sync with its rendered size.
     * Call this right after constructing the handler (BookController.startSession).
     */
    public void setPdfView(ImageView pdfView) {
        this.pdfView = pdfView;
        bindCanvasToImageView();
    }

    /**
     * Keeps the canvas width/height equal to the ImageView's actual rendered
     * image bounds (not its fitWidth — those may differ when preserveRatio=true).
     * We listen to both boundsInParent changes so we react to zoom and page flips.
     */
    private void bindCanvasToImageView() {
        if (pdfView == null || highlightCanvas == null) return;

        pdfView.boundsInParentProperty().addListener((obs, oldB, newB) -> syncCanvasSize());
        // also sync immediately in case the image is already loaded
        syncCanvasSize();
    }

    private void syncCanvasSize() {
        if (pdfView == null || highlightCanvas == null) return;
        double w = pdfView.getBoundsInParent().getWidth();
        double h = pdfView.getBoundsInParent().getHeight();
        if (w > 0 && h > 0) {
            highlightCanvas.setWidth(w);
            highlightCanvas.setHeight(h);
            // Reposition: canvas must overlay the ImageView exactly.
            // Both live inside the same StackPane so no offset is needed —
            // but we do need to redraw after a resize.
            drawHighlightsForPage(currentPage);
        }
    }

    public void enableHighlight() {
        if (highlightCanvas == null) return;
        GraphicsContext gc = highlightCanvas.getGraphicsContext2D();
        highlightCanvas.setCursor(javafx.scene.Cursor.CROSSHAIR);

        highlightCanvas.setOnMousePressed(e -> {
            dragStartX = e.getX();
            dragStartY = e.getY();
        });

        highlightCanvas.setOnMouseDragged(e -> {
            gc.clearRect(0, 0, highlightCanvas.getWidth(), highlightCanvas.getHeight());
            redrawSaved(gc, currentPage);
            double x = Math.min(dragStartX, e.getX());
            double y = Math.min(dragStartY, e.getY());
            double w = Math.abs(e.getX() - dragStartX);
            double h = Math.abs(e.getY() - dragStartY);
            gc.setFill(Color.rgb(255, 255, 0, 0.35));
            gc.fillRect(x, y, w, h);
        });

        highlightCanvas.setOnMouseReleased(e -> {
            double x = Math.min(dragStartX, e.getX());
            double y = Math.min(dragStartY, e.getY());
            double w = Math.abs(e.getX() - dragStartX);
            double h = Math.abs(e.getY() - dragStartY);
            if (w > 5 && h > 5) {
                highlights.computeIfAbsent(currentPage, k -> new ArrayList<>())
                        .add(new double[]{x, y, w, h});
            }
        });
    }

    public void drawHighlightsForPage(int page) {
        if (highlightCanvas == null) return;
        GraphicsContext gc = highlightCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, highlightCanvas.getWidth(), highlightCanvas.getHeight());
        redrawSaved(gc, page);
    }

    private void redrawSaved(GraphicsContext gc, int page) {
        List<double[]> saved = highlights.get(page);
        if (saved != null) {
            gc.setFill(Color.rgb(255, 255, 0, 0.35));
            for (double[] r : saved) gc.fillRect(r[0], r[1], r[2], r[3]);
        }
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }
}