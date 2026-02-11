package org.example.bookreader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
public class Library {
    private static final String File_Name="LibraryData.json";
    private static final ObjectMapper mapper =new ObjectMapper();
    //saving the full list to a file
    public static void saveBookList(List<Book>books){
        try{
            mapper.writeValue(new File(File_Name),books);
        }catch(Exception e){
            System.err.println("Error saving: "+e.getMessage());
        }

    }
    //loading the list from that file
    public static List<Book> loadBooks() {
        File f = new File(File_Name);
        if (!f.exists()) {
//empty list if no file exists yet
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(f, new TypeReference<List<Book>>(){});

        }catch (Exception e){
            return new ArrayList<>();
        }
    }
}