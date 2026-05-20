package spengergasse.at;

public abstract class contentEntry {
        private int id;
        private String author;
        private String date;
        private boolean visible;
        private String category;

//Construcktor
    public contentEntry(int id, String author, boolean visible, String date, String category) {
        this.id = id;
        this.author = author;
        this.visible = visible;
        this.date = date;
        this.category = category;
    }


    //Setter
    public void setId(int id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }


//Getter
    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getCategory() {
        return category;
    }

    public void validate() throws InvalidInputException{
        if (author == null || author.isEmpty()) {
            throw new InvalidInputException("Author cannot be empty");
        }
        if (date == null || date.isEmpty()) {
            throw new InvalidInputException("Date cannot be empty");
        }
        if (category == null || category.isEmpty()) {
            throw new InvalidInputException("Category cannot be empty");
        }
        if (id  <= 0) {
            throw new InvalidInputException("Id cannot be negative");
        }
    }
}

