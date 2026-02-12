package org.example.bookreader;
import javafx.scene.image.Image;

import java.io.IOException;

public class FileTypeManager {
    //2 possible types
    private PDFEngine pdfEngine;
    private ComicEngine comicEngine;
    private String currentFileType;
    public void fileType(String filePath) throws IOException {
        String lowerCase=filePath.toLowerCase();
        if(lowerCase.endsWith(".pdf")){
            pdfEngine=new PDFEngine(filePath);
            currentFileType="pdf";
        }
        else if(lowerCase.endsWith(".cbz") || lowerCase.endsWith(".zip")){
            comicEngine = new ComicEngine(filePath);
            currentFileType="comic";
        }
    }
    public Image getPage(int pageNumber){
        if(currentFileType.equals("pdf")){
            return pdfEngine.renderingPage(pageNumber);
        }
        else{
            return comicEngine.getPage(pageNumber);
        }
    }
    public int getTotalPage(){
        if(currentFileType.equals("pdf")){
            return pdfEngine.getPageCount();
        }
        else{
            return comicEngine.getTotalPages();
        }
    }
}
