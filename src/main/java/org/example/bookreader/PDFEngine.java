package org.example.bookreader;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFEngine {
    private final PDDocument document;
    private final PDFRenderer renderer;

    public PDFEngine(String filePath) throws IOException {
        this.document = Loader.loadPDF(new File(filePath));
        this.renderer = new PDFRenderer(document);
        // tells PDFBox to use the system's AWT image subsystem for JPEG/CCITT decoding
        renderer.setSubsamplingAllowed(false);
    }



    public Image renderingPage(int pageNum) {
        try {
            // RGB at 150 DPI — good quality, reasonable memory
            BufferedImage bufferedIm = renderer.renderImageWithDPI(pageNum, 150, ImageType.RGB);
            return SwingFXUtils.toFXImage(bufferedIm, null);
        } catch (Exception e) {
            // print full stack trace so we can see exactly what failed
            System.err.println("=== PDFEngine render error on page " + pageNum + " ===");
            e.printStackTrace();

            // fallback: try ARGB mode — some PDFs with transparency need this
            try {
                System.err.println("Trying ARGB fallback for page " + pageNum);
                BufferedImage fallback = renderer.renderImageWithDPI(pageNum, 120, ImageType.ARGB);
                return SwingFXUtils.toFXImage(fallback, null);
            } catch (Exception e2) {
                System.err.println("ARGB fallback also failed: " + e2.getMessage());
                return null;
            }
        }
    }


    public int getPageCount() {
        if (document == null) return 0;
        return document.getNumberOfPages();
    }

    // close method to release memory
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
