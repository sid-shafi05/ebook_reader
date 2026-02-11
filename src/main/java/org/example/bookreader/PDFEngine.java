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
    private PDFRenderer renderer;
    private final String filePath; // Remember the path we opened

    // --- CONSTRUCTOR (This is the new "Open File" part) ---
    public PDFEngine(String filePath) throws IOException {
        this.filePath = filePath;
        this.document = PDDocument.load(new File(filePath));
        this.renderer = new PDFRenderer(document);
    }

    // --- METHOD TO GET A PAGE (Now faster, as the file is already open) ---
    // We rename it to match your partner's code structure (renderingPage) but adjust parameters
    public Image renderingPage(int pageNum) {
        try {
            // We use the 'renderer' that was created in the constructor!
            BufferedImage bufferedIm = renderer.renderImageWithDPI(pageNum, 300);
            return SwingFXUtils.toFXImage(bufferedIm, null);
        } catch (Exception e) {
            System.err.println("Error rendering PDF page: " + e.getMessage());
            return null;
        }
    }

    // --- METHOD TO GET PAGE COUNT ---
    public int getPageCount() {
        if (document == null) return 0;
        return document.getNumberOfPages();
    }

    // --- THE MISSING 'CLOSE' METHOD ---
    public void close() {
        try {
            if (document != null) {
                document.close();
                System.out.println("PDF Document successfully closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

