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
private  PDDocument document;
private PDFRenderer pdfRenderer;
private String filePath;
public PDFEngine(String filePath) throws IOException{
    this.filePath=filePath;
    //open the document once
    this.document=PDDocument.load(new File(filePath));
    this.pdfRenderer=new PDFRenderer(document);
}
public Image getPageImage(int pageNum){
    try{
        BufferedImage bufferedImage=pdfRenderer.renderImageWithDPI(pageNum,200);
        return SwingFXUtils.toFXImage(bufferedImage,null);
    } catch (IOException e){
        System.err.println("Failed to render page "+pageNum);
        return null;
    }
}
public int getPageCount(){
    return document.getNumberOfPages();
}
public void close(){
    try{
        if(document!=null){
            document.close();//closes the document to free up RAM
        }
    }catch(IOException e){
e.printStackTrace();
    }
}
    /*public String getAuthorMetadata() {

        String author = document.getDocumentInformation().getAuthor();

        // If the PDF doesn't have an author tag, return "Unknown"
        return (author != null && !author.isEmpty()) ? author : "Unknown Author";
    }*/
}
