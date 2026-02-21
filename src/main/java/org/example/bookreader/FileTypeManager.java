package org.example.bookreader;
import javafx.scene.image.Image;
import java.io.IOException;

public class FileTypeManager {
    private PDFEngine pdfEngine;
    private ComicEngine comicEngine;
    private String currentFileType;

    public void fileType(String filePath) throws IOException {
        String lowerCase = filePath.toLowerCase();
        if (lowerCase.endsWith(".pdf")) {
            pdfEngine = new PDFEngine(filePath);
            currentFileType = "pdf";
        } else if (lowerCase.endsWith(".cbz") || lowerCase.endsWith(".zip")) {
            comicEngine = new ComicEngine(filePath);
            currentFileType = "comic";
        }
    }

    public Image getPage(int pageNumber) {
        if ("pdf".equals(currentFileType) && pdfEngine != null) {
            return pdfEngine.renderingPage(pageNumber);
        } else if ("comic".equals(currentFileType) && comicEngine != null) {
            return comicEngine.getPage(pageNumber);
        }
        return null;
    }

    public int getTotalPage() {
        if ("pdf".equals(currentFileType) && pdfEngine != null) {
            return pdfEngine.getPageCount();
        } else if ("comic".equals(currentFileType) && comicEngine != null) {
            return comicEngine.getTotalPages();
        }
        return 0; // safe fallback
    }

    // release PDF memory when the reader window closes
    public void close() {
        if ("pdf".equals(currentFileType) && pdfEngine != null) {
            pdfEngine.close();
        }
        // CBZ images are held in memory by ComicEngine — just let GC collect them
    }
}

