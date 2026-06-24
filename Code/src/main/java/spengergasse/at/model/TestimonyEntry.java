package spengergasse.at.model;

import spengergasse.at.util.InvalidInputException;

/**
 * Subklasse für Glaubenszeugnisse (24h sichtbar).
 * Repräsentiert persönliche Erfahrungen mit Gott.
 */
public class TestimonyEntry extends ContentEntry {

    private String title;
    private String story;
    private boolean expired;      // nach 24h abgelaufen
    private int likeCount;

    // ── Konstruktor ──────────────────────────────────────────────────────────

    public TestimonyEntry(int id, String author, String date, boolean visible,
                          String category, String title, String story)
            throws InvalidInputException {
        super(id, author, date, visible, category);
        setTitle(title);
        setStory(story);
        this.expired = false;
        this.likeCount = 0;
    }

    // ── Setter mit Validierung ────────────────────────────────────────────────

    public void setTitle(String title) throws InvalidInputException {
        if (title == null || title.isBlank())
            throw new InvalidInputException("Titel darf nicht leer sein");
        if (title.length() > 150)
            throw new InvalidInputException("Titel darf max. 150 Zeichen haben");
        this.title = title.trim();
    }

    public void setStory(String story) throws InvalidInputException {
        if (story == null || story.isBlank())
            throw new InvalidInputException("Zeugnis darf nicht leer sein");
        if (story.length() > 3000)
            throw new InvalidInputException("Zeugnis darf max. 3000 Zeichen haben");
        this.story = story.trim();
    }

    public void setExpired(boolean expired) { this.expired = expired; }

    public void setLikeCount(int likeCount) throws InvalidInputException {
        if (likeCount < 0)
            throw new InvalidInputException("Like-Anzahl darf nicht negativ sein");
        this.likeCount = likeCount;
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public String getTitle()    { return title; }
    public String getStory()    { return story; }
    public boolean isExpired()  { return expired; }
    public int getLikeCount()   { return likeCount; }

    // ── Eigene Methoden ───────────────────────────────────────────────────────

    /**
     * Prüft ob das Zeugnis innerhalb der letzten 24 Stunden ist.
     */
    public boolean isActive() {
        return !expired && isWithinDays(1);
    }

    /**
     * Fügt einen Like hinzu.
     */
    public void addLike() { this.likeCount++; }

    /**
     * Markiert das Zeugnis als abgelaufen.
     */
    public void expire() { this.expired = true; }

    // ── Abstrakte Methoden implementiert ─────────────────────────────────────

    @Override
    public String formatContent() {
        String status = expired ? "[ABGELAUFEN]" : "[AKTIV]";
        return String.format("✨ %s %s – %s (❤️ %d)", title, status, story, likeCount);
    }

    @Override
    public String toCsvString() {
        return String.format("TESTIMONY;%d;%s;%s;%b;%s;%s;%s;%b;%d",
                getId(), getAuthor(), getDateString(), isVisible(),
                getCategory(), title,
                story.replace(";", ","),
                expired, likeCount);
    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && title != null && !title.isBlank()
                && story != null && !story.isBlank();
    }

    @Override
    public String toString() {
        return String.format("%s | %s [abgelaufen=%b]", super.toString(), title, expired);
    }
}
