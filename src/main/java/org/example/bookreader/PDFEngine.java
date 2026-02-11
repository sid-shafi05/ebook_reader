
package org.example.bookreader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
public class PDFEngine {
    //takes the file path and page number of the book
    //renders it to a buffered image
    //converts it into JavaFX image and returns it
    public Image renderingPage(String filePath, int pageNum) {
        try (PDDocument doc = PDDocument.load(new File(filePath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            BufferedImage bufferedIm = pdfRenderer.renderImageWithDPI(pageNum, 300);
            return SwingFXUtils.toFXImage(bufferedIm, null);
        } catch (Exception e) {
            System.err.println("Error rendering PDF page: " + e.getMessage());
            return null;
        }
    }

    public int getPageNumber(String filePath) {
        try (PDDocument doc = PDDocument.load(new File(filePath))) {
            return doc.getNumberOfPages();
        } catch (IOException e) {
            return 0;
        }
    }
}

