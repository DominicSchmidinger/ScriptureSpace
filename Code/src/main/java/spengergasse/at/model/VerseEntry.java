package spengergasse.at.model;

import spengergasse.at.util.InvalidInputException;

/**
 * Subklasse für Bibelvers-Einträge.
 * Enthält Buch, Kapitel, Versnummer und den Verstext.
 */
public class VerseEntry extends ContentEntry {

    private String book;
    private int chapter;
    private int verseNumber;
    private String verse;

    // ── Konstruktor ──────────────────────────────────────────────────────────

    public VerseEntry(int id, String author, String date, boolean visible,
                      String category, String book, int chapter, int verseNumber, String verse)
            throws InvalidInputException {
        super(id, author, date, visible, category);
        setBook(book);
        setChapter(chapter);
        setVerseNumber(verseNumber);
        setVerse(verse);
    }

    // ── Setter mit Validierung ────────────────────────────────────────────────

    public void setBook(String book) throws InvalidInputException {
        if (book == null || book.isBlank())
            throw new InvalidInputException("Buch darf nicht leer sein");
        this.book = book.trim();
    }

    public void setChapter(int chapter) throws InvalidInputException {
        if (chapter <= 0)
            throw new InvalidInputException("Kapitel muss größer als 0 sein, war: " + chapter);
        this.chapter = chapter;
    }

    public void setVerseNumber(int verseNumber) throws InvalidInputException {
        if (verseNumber <= 0)
            throw new InvalidInputException("Versnummer muss größer als 0 sein, war: " + verseNumber);
        this.verseNumber = verseNumber;
    }

    public void setVerse(String verse) throws InvalidInputException {
        if (verse == null || verse.isBlank())
            throw new InvalidInputException("Verstext darf nicht leer sein");
        if (verse.length() > 1000)
            throw new InvalidInputException("Verstext darf max. 1000 Zeichen haben");
        this.verse = verse.trim();
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public String getBook()      { return book; }
    public int getChapter()      { return chapter; }
    public int getVerseNumber()  { return verseNumber; }
    public String getVerse()     { return verse; }

    // ── Eigene Methoden ───────────────────────────────────────────────────────

    /**
     * Gibt die Bibelstellen-Referenz zurück, z. B. "Johannes 3:16"
     */
    public String getReference() {
        return String.format("%s %d:%d", book, chapter, verseNumber);
    }

    /**
     * Gibt an ob der Vers aus dem Neuen Testament stammt (einfache Heuristik).
     */
    public boolean isNewTestament() {
        String[] ntBooks = {"Matthäus", "Markus", "Lukas", "Johannes", "Apostelgeschichte",
                "Römer", "Korinther", "Galater", "Epheser", "Philipper",
                "Kolosser", "Thessalonicher", "Timotheus", "Titus", "Philemon",
                "Hebräer", "Jakobus", "Petrus", "Judas", "Offenbarung"};
        for (String nt : ntBooks) {
            if (book.contains(nt)) return true;
        }
        return false;
    }

    // ── Abstrakte Methoden implementiert ─────────────────────────────────────

    @Override
    public String formatContent() {
        return String.format("📖 %s %d:%d – \"%s\"", book, chapter, verseNumber, verse);
    }

    @Override
    public String toCsvString() {
        return String.format("VERSE;%d;%s;%s;%b;%s;%s;%d;%d;%s",
                getId(), getAuthor(), getDateString(), isVisible(),
                getCategory(), book, chapter, verseNumber,
                verse.replace(";", ","));
    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && book != null && !book.isBlank()
                && chapter > 0
                && verseNumber > 0
                && verse != null && !verse.isBlank();
    }

    @Override
    public String toString() {
        return String.format("%s | %s", super.toString(), getReference());
    }
}
