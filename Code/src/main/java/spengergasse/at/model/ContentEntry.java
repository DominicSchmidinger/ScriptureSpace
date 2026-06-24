package spengergasse.at.model;

import spengergasse.at.util.InvalidInputException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Abstrakte Superklasse für alle Inhaltstypen in Scripture Space.
 * Enthält gemeinsame Attribute und Logik für alle Einträge.
 */
public abstract class ContentEntry {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private int id;
    private String author;
    private LocalDate date;
    private boolean visible;
    private String category;
    private boolean favorite;

    // ── Konstruktor ──────────────────────────────────────────────────────────

    public ContentEntry(int id, String author, String date, boolean visible, String category)
            throws InvalidInputException {
        setId(id);
        setAuthor(author);
        setDate(date);
        setVisible(visible);
        setCategory(category);
        this.favorite = false;
    }

    // ── Setter mit Validierung ────────────────────────────────────────────────

    public void setId(int id) throws InvalidInputException {
        if (id <= 0) throw new InvalidInputException("ID muss größer als 0 sein, war: " + id);
        this.id = id;
    }

    public void setAuthor(String author) throws InvalidInputException {
        if (author == null || author.isBlank())
            throw new InvalidInputException("Autor darf nicht leer sein");
        if (author.length() > 100)
            throw new InvalidInputException("Autor darf max. 100 Zeichen haben");
        this.author = author.trim();
    }

    public void setDate(String date) throws InvalidInputException {
        if (date == null || date.isBlank())
            throw new InvalidInputException("Datum darf nicht leer sein");
        try {
            this.date = LocalDate.parse(date, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Ungültiges Datum – Format: dd.MM.yyyy, war: " + date);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setCategory(String category) throws InvalidInputException {
        if (category == null || category.isBlank())
            throw new InvalidInputException("Kategorie darf nicht leer sein");
        this.category = category.trim();
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public int getId()           { return id; }
    public String getAuthor()    { return author; }
    public LocalDate getDate()   { return date; }
    public String getDateString(){ return date.format(DATE_FORMAT); }
    public boolean isVisible()   { return visible; }
    public String getCategory()  { return category; }
    public boolean isFavorite()  { return favorite; }

    // ── Methoden mit Logik ────────────────────────────────────────────────────

    /**
     * Prüft ob der Eintrag vollständig und gültig ist.
     */
    public boolean isValid() {
        return id > 0
                && author != null && !author.isBlank()
                && date != null
                && category != null && !category.isBlank();
    }

    /**
     * Gibt an ob der Eintrag heute erstellt wurde.
     */
    public boolean isToday() {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Gibt an ob der Eintrag innerhalb der letzten n Tage erstellt wurde.
     */
    public boolean isWithinDays(int days) {
        return date != null && !date.isBefore(LocalDate.now().minusDays(days));
    }

    // ── Abstrakte Methoden ────────────────────────────────────────────────────

    /** Formatiert den Inhalt für die Anzeige. */
    public abstract String formatContent();

    /** Serialisiert den Eintrag als CSV-Zeile. */
    public abstract String toCsvString();

    // ── Object Overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentEntry)) return false;
        return id == ((ContentEntry) o).id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | sichtbar=%b",
                id, getClass().getSimpleName(), author, getDateString(), visible);
    }
}
