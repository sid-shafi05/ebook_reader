# PageVault — eBook Reader

A JavaFX-based desktop ebook reader supporting PDF and CBZ files, with reading statistics, bookmarks, notes, and an AI chat assistant powered by the Groq LLM API.

---

## Prerequisites

Before running the project, make sure you have the following installed:

- **Java 21** (JDK 21 or later) — [Download here](https://adoptium.net/)
- **Maven 3.8+** — or use the included `mvnw` wrapper (no separate install needed)
- An IDE like **IntelliJ IDEA** is recommended but not required

---

## 1. Clone or Download the Project

```bash
git clone <your-repo-url>
cd BookReader
```

Or simply extract the project ZIP into a folder of your choice.

---

## 2. Set Up the Groq API Key (`config.properties`)

The AI chat feature inside the reader uses the **Groq LLM API** (model: `llama-3.1-8b-instant`). You need to create a `config.properties` file with your API key.

### Step 1 — Get a Groq API Key

1. Go to [https://console.groq.com](https://console.groq.com) and sign up or log in.
2. Navigate to **API Keys** in the left sidebar.
3. Click **Create API Key**, give it a name, and copy the key.

### Step 2 — Create the config file

Create a file named `config.properties` at the following path inside the project:

```
src/main/resources/config.properties
```

The file should contain exactly this (replace the placeholder with your real key):

```properties
api.key=gsk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

> ⚠️ **Never commit this file to version control.** Add it to your `.gitignore`:
> ```
> src/main/resources/config.properties
> ```

---

## 3. Build the Project

Using the Maven wrapper (no Maven installation needed):

**On macOS / Linux:**
```bash
./mvnw clean install -DskipTests
```

**On Windows:**
```cmd
mvnw.cmd clean install -DskipTests
```

---

## 4. Run the Application

**On macOS / Linux:**
```bash
./mvnw javafx:run
```

**On Windows:**
```cmd
mvnw.cmd javafx:run
```

Alternatively, if you are using **IntelliJ IDEA**:

1. Open the project (`File → Open` → select the project folder).
2. Let IntelliJ import the Maven project and download dependencies.
3. Run `org.example.bookreader.Launcher` as the main class.

---

## 5. First Launch & Adding Books

On first launch, PageVault will automatically create the required data folders on your Desktop:

```
~/Desktop/ebook_project_data/
    booksdata/     ← where your PDF/CBZ files are copied to
    covers/        ← auto-generated cover thumbnails
```

To add a book:
1. Click **+ Add Book** in the top bar.
2. Select a `.pdf` or `.cbz` file.
3. Choose a genre/category from the dropdown.
4. The book will appear in your library.

---

## 6. Project Data Files

The following files are created/used in the **project root directory** at runtime:

| File | Purpose |
|---|---|
| `LibraryData.json` | Stores your book library metadata |
| `bookmarks.json` | Stores all bookmarks across books |
| `stats.json` | Reading session history |
| `daily_target.txt` | Your daily reading goal in minutes |
| `notes/` | Per-book notes (`.txt` files) |

---

## 7. Features Overview

- 📚 **Library** — Browse, search, sort, and manage your books
- ♡ **Favourites** — Mark books as favourite
- ⊞ **Categories** — Browse books grouped by genre
- ◈ **Statistics** — View reading time charts, page counts, and daily goals
- **Reader** — PDF & CBZ rendering with zoom, page slider, and focus mode
- **Bookmarks** — Add and jump to bookmarked pages
- **Notes** — Per-book notebook panel
- **AI Chat** — Ask questions about the book you're reading (powered by Groq)

---

## Troubleshooting

**App won't start / JavaFX errors:**
Make sure you are using **Java 21**. Check with:
```bash
java -version
```

**AI chat returns an error:**
- Verify `config.properties` exists at `src/main/resources/config.properties`
- Confirm the key starts with `gsk_` and has no extra spaces
- Check your internet connection and that your Groq account is active

**Book cover doesn't load:**
The cover image is generated from the first page of the book and saved to `~/Desktop/ebook_project_data/covers/`. If the file is missing, try removing and re-adding the book.

**On Linux — path issues with existing `LibraryData.json`:**
If you have entries with Windows-style paths (`C:\Users\...`), you may need to clear `LibraryData.json` and re-add your books for the paths to resolve correctly on Linux.
