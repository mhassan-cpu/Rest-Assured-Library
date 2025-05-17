package pojo;

public class CreateBook {

    String title;

    public String getTitle() {
        return title;
    }

    public CreateBook setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public CreateBook setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getIsbn() {
        return isbn;
    }

    public CreateBook setIsbn(String isbn) {
        this.isbn = isbn;
        return this;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public CreateBook setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    String author;
    String isbn;
    String releaseDate;
}
