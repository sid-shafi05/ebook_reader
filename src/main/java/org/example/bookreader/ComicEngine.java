package org.example.bookreader;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ComicEngine {
    //list or binder to hold all the pages
    private List<Image> pages = new ArrayList<>();

    private void loadComic(String filePath){
        try(ZipFile zipFile=new ZipFile(filePath)){
            List<ZipEntry> entries = new ArrayList<>();
            var enumeration =zipFile.entries();
            while(enumeration.hasMoreElements()){
                entries.add(enumeration.nextElement());
            }
            //sort the entries so page 1 comes before page 2
            entries.sort(Comparator.comparing(ZipEntry::getName));
            for(ZipEntry entry:entries){
                String fileName=entry.getName().toLowerCase();
                if(fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")){
                    try(InputStream iStream = zipFile.getInputStream(entry)){
                        Image pageImage=new Image(iStream);
                        pages.add(pageImage);
                    }
                }
            }
            System.out.println("Comic loaded. Total pages: "+pages.size());
        } catch (Exception e){
            System.err.println("Comic failed to load: "+ e.getMessage());
        }
    }

public ComicEngine(String filePath){
    loadComic(filePath);
}
public Image getPage(int pageNumber){
    if(pageNumber>=0 && pageNumber<pages.size()){
         return pages.get(pageNumber);
    }
    return null;
}
public int getTotalPages(){
        return pages.size();
}
}

