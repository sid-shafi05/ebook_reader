package org.example.bookreader;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighlightHandler {
    private Canvas highlightCanvas;
    private Map<Integer, List<double[]>> highlights = new HashMap<>();
    private double dragStartX, dragStartY;

    public HighlightHandler(Canvas highlightCanvas) {
        this.highlightCanvas = highlightCanvas;
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
            redrawSaved(gc, getCurrentPageFromCanvas());
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
                int page = getCurrentPageFromCanvas();
                highlights.computeIfAbsent(page, k -> new java.util.ArrayList<>())
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

    // Canvas doesn't know the current page — BookController passes it via drawHighlightsForPage
    // This is a fallback for internal use during drag
    private int currentPage = 0;
    public void setCurrentPage(int page) { this.currentPage = page; }
    private int getCurrentPageFromCanvas() { return currentPage; }
}