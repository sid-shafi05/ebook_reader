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
    public static void saveBookList(List<Book>books){
        try{
            mapper.writeValue(new File(File_Name),books);
        }catch(Exception e){
            System.err.println("Error saving: "+e.getMessage());
            e.printStackTrace();
        }

    }
    public static List<Book> loadBooks() {
        File f = new File(File_Name);
        if (!f.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(f, new TypeReference<List<Book>>(){});

        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    public static List<Book> getFavouriteBooks() {
        List<Book> result = new ArrayList<>();
        for (Book b : loadBooks()) {
            if (b.isFavourite()) result.add(b);
        }
        return result;
    }

    public static List<String> getAllCategories() {
        List<String> cats = new ArrayList<>();
        for (Book b : loadBooks()) {
            String c = b.getCategory();
            if (c != null && !c.isEmpty() && !cats.contains(c))
                cats.add(c);
        }
        return cats;
    }

    public static List<Book> getBooksByCategory(String category) {
        List<Book> result = new ArrayList<>();
        for (Book b : loadBooks()) {
            if (category.equals(b.getCategory())) result.add(b);
        }
        return result;
    }
}