package spengergasse.at;

public class VerseEntry extends contentEntry {
    private String book;
    private int chapter;
    private int verseNumber;


    //Prof fragen
    public VerseEntry(int id, String author, boolean visible, String date, String category)throws InvalidInputException {
        super(id, author, visible, date, category);
        setBook(author);
        setChapter(chapter);
        setVerseNumber(verseNumber);
    }

    //Setter
    public void setBook(String book) throws InvalidInputException {
        if (book == null || book.isBlank()) {
            //buch aus der bibel
            throw new InvalidInputException("Please add a Book from the bibel");
        }
        this.book = book;
    }

    public void setChapter(int chapter) throws InvalidInputException {
        if (chapter <= 0) {
            throw new InvalidInputException("Pleade fill in the Chapter");
        }
        //if capitel name
        //if abhängig von kapitel name verse von bis
        this.chapter = chapter;
    }

    public void setVerseNumber(int verseNumber) throws InvalidInputException {
        if (verseNumber <= 0) {
            throw new InvalidInputException("Please fill in the verse number");
        }
        this.verseNumber = verseNumber;
    }


    //Getter
    public String getBook() {
        return book;
    }

    public int getChapter() {
        return chapter;
    }

    public int getVerseNumber() {
        return verseNumber;
    }
}
