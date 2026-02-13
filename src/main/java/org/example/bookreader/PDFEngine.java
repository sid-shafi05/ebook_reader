package org.example.bookreader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFEngine {
    private PDDocument document;
    private PDFRenderer pdfRenderer;
    private String filePath;

    // Constructor that takes file path
    public PDFEngine(String filePath) throws IOException {
        this.filePath = filePath;
        this.document = PDDocument.load(new File(filePath));
        this.pdfRenderer = new PDFRenderer(document);
    }

    // Default constructor (for backward compatibility)
    public PDFEngine() {}

    // Render a specific page (instance method for FileTypeManager)
    public Image renderingPage(int pageNum) {
        try {
            BufferedImage bufferedIm = pdfRenderer.renderImageWithDPI(pageNum, 300);
            return SwingFXUtils.toFXImage(bufferedIm, null);
        } catch (Exception e) {
            System.err.println("Error rendering PDF page: " + e.getMessage());
            return null;
        }
    }

    // Static method for cover generation (backward compatibility)
    public static Image renderingPage(String filePath, int pageNum) {
        try (PDDocument doc = PDDocument.load(new File(filePath))) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage bufferedIm = renderer.renderImageWithDPI(pageNum, 300);
            return SwingFXUtils.toFXImage(bufferedIm, null);
        } catch (Exception e) {
            System.err.println("Error rendering PDF page: " + e.getMessage());
            return null;
        }
    }

    // Get page count (instance method)
    public int getPageCount() {
        return document.getNumberOfPages();
    }

    // Static method for getting page count (backward compatibility)
    public static int getPageNumber(String filePath) {
        try (PDDocument doc = PDDocument.load(new File(filePath))) {
            return doc.getNumberOfPages();
        } catch (IOException e) {
            return 0;
        }
    }

    // Close document when done
    public void close() {
        try {
            if (document != null) {
                document.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}