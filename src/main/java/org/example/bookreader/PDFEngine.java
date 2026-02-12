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
    private final String filePath;

    // constructor to load the pdf ONCE, so less memory consumption and faster laoding
    public PDFEngine(String filePath) throws IOException {
        this.filePath = filePath;
        this.document = PDDocument.load(new File(filePath));
        this.renderer = new PDFRenderer(document);
    }



    public Image renderingPage(int pageNum) {
        try {

            BufferedImage bufferedIm = renderer.renderImageWithDPI(pageNum, 300);
            return SwingFXUtils.toFXImage(bufferedIm, null);
        } catch (Exception e) {
            System.err.println("Error rendering PDF page: " + e.getMessage());
            return null;
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

