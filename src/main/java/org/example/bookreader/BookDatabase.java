package org.example.bookreader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookDatabase {
    private static BookDatabase instance;
    private ObservableList<Book> books;

    // Fixed path - always resolves to same place regardless of working directory
    private static final String DATABASE_FILE =
            System.getProperty("user.home") + File.separator + ".stackshelf" + File.separator + "books_database.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    private BookDatabase() {
        books = FXCollections.observableArrayList();
        ensureDirectoryExists();
        loadFromFile();
    }

    public static BookDatabase getInstance() {
        if (instance == null) {
            instance = new BookDatabase();
        }
        return instance;
    }

    private void ensureDirectoryExists() {
        File dir = new File(DATABASE_FILE).getParentFile();
        if (!dir.exists()) dir.mkdirs();
    }

    private void loadFromFile() {
        File file = new File(DATABASE_FILE);
        if (!file.exists()) {
            System.out.println("No database file found. Starting fresh.");
            return;
        }
        try {
            List<BookData> dataList = mapper.readValue(file, new TypeReference<List<BookData>>() {});
            for (BookData d : dataList) {
                Book book = new Book(d.title != null ? d.title : "Unknown",
                        d.filePath != null ? d.filePath : "");
                book.setAuthorName(d.authorName != null ? d.authorName : "Unknown Author");
                book.setCategory(d.category != null ? d.category : "Uncategorized");
                book.setTotalPages(d.totalPages);
                book.setLastReadPageNumber(d.lastReadPageNumber);
                book.setFavouriteStatus(d.isFavourite);
                book.setCoverPath(d.coverPath);
                book.setProgressValue(d.progressValue);
                book.setDateAdded(d.dateAdded != 0 ? d.dateAdded : System.currentTimeMillis());
                books.add(book);
            }
            System.out.println("Loaded " + books.size() + " books from " + DATABASE_FILE);
        } catch (IOException e) {
            System.err.println("Error loading database: " + e.getMessage());
        }
    }

    public void saveToFile() {
        try {
            List<BookData> dataList = new ArrayList<>();
            for (Book book : books) {
                BookData d = new BookData();
                d.title             = book.getTitle();
                d.filePath          = book.getFilePath();
                d.authorName        = book.getAuthorName();
                d.category          = book.getCategory();
                d.totalPages        = book.getTotalPages();
                d.lastReadPageNumber = book.getLastReadPageNumber();
                d.isFavourite       = book.isFavourite();
                d.coverPath         = book.getCoverPath();
                d.progressValue     = book.getProgressValue();
                d.dateAdded         = book.getDateAdded();
                dataList.add(d);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATABASE_FILE), dataList);
        } catch (IOException e) {
            System.err.println("Error saving database: " + e.getMessage());
        }
    }

    public void addBook(Book book) {
        if (getBookByPath(book.getFilePath()) != null) return; // no duplicates
        books.add(book);
        saveToFile();
        System.out.println("Book added: " + book.getTitle() + " | Total: " + books.size());
    }

    public void removeBook(Book book) {
        books.remove(book);
        saveToFile();
    }

    public void updateBook(Book book) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getFilePath().equals(book.getFilePath())) {
                books.set(i, book);
                break;
            }
        }
        saveToFile();
    }

    public ObservableList<Book> getAllBooks()    { return books; }
    public int getBookCount()                    { return books.size(); }

    public Book getBookByPath(String filePath) {
        for (Book b : books) {
            if (b.getFilePath().equals(filePath)) return b;
        }
        return null;
    }

    public List<String> getAllCategories() {
        List<String> cats = new ArrayList<>();
        for (Book b : books) {
            String c = b.getCategory();
            if (c != null && !c.isEmpty() && !cats.contains(c)) cats.add(c);
        }
        return cats;
    }

    public List<Book> getBooksByCategory(String category) {
        List<Book> result = new ArrayList<>();
        for (Book b : books) {
            if (category.equals(b.getCategory())) result.add(b);
        }
        return result;
    }

    public List<Book> getFavouriteBooks() {
        List<Book> result = new ArrayList<>();
        for (Book b : books) {
            if (b.isFavourite()) result.add(b);
        }
        return result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BookData {
        public String title;
        public String filePath;
        public String authorName;
        public String category;
        public int totalPages;
        public int lastReadPageNumber;
        public boolean isFavourite;
        public String coverPath;
        public double progressValue;
        public long dateAdded;
    }
}