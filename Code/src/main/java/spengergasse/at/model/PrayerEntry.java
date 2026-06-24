package spengergasse.at.model;

import spengergasse.at.util.InvalidInputException;

/**
 * Subklasse für Gebets-Einträge.
 * Repräsentiert ein geteiltes Gebet in der Gemeinschaft.
 */
public class PrayerEntry extends ContentEntry {

    public enum PrayerStatus { OFFEN, ERHÖRT, LAUFEND }

    private String title;
    private String prayerText;
    private PrayerStatus status;
    private int prayerCount; // wie viele Leute haben mitgebetet

    // ── Konstruktor ──────────────────────────────────────────────────────────

    public PrayerEntry(int id, String author, String date, boolean visible,
                       String category, String title, String prayerText)
            throws InvalidInputException {
        super(id, author, date, visible, category);
        setTitle(title);
        setPrayerText(prayerText);
        this.status = PrayerStatus.OFFEN;
        this.prayerCount = 0;
    }

    // ── Setter mit Validierung ────────────────────────────────────────────────

    public void setTitle(String title) throws InvalidInputException {
        if (title == null || title.isBlank())
            throw new InvalidInputException("Titel darf nicht leer sein");
        if (title.length() > 150)
            throw new InvalidInputException("Titel darf max. 150 Zeichen haben");
        this.title = title.trim();
    }

    public void setPrayerText(String prayerText) throws InvalidInputException {
        if (prayerText == null || prayerText.isBlank())
            throw new InvalidInputException("Gebetstext darf nicht leer sein");
        if (prayerText.length() > 2000)
            throw new InvalidInputException("Gebetstext darf max. 2000 Zeichen haben");
        this.prayerText = prayerText.trim();
    }

    public void setStatus(PrayerStatus status) throws InvalidInputException {
        if (status == null) throw new InvalidInputException("Status darf nicht null sein");
        this.status = status;
    }

    public void setPrayerCount(int prayerCount) throws InvalidInputException {
        if (prayerCount < 0)
            throw new InvalidInputException("Gebetsanzahl darf nicht negativ sein");
        this.prayerCount = prayerCount;
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public String getTitle()        { return title; }
    public String getPrayerText()   { return prayerText; }
    public PrayerStatus getStatus() { return status; }
    public int getPrayerCount()     { return prayerCount; }

    // ── Eigene Methoden ───────────────────────────────────────────────────────

    /**
     * Erhöht den Zähler "Ich bete mit" um 1.
     */
    public void addPrayer() {
        this.prayerCount++;
    }

    /**
     * Markiert das Gebet als erhört.
     */
    public void markAsAnswered() {
        this.status = PrayerStatus.ERHÖRT;
    }

    /**
     * Gibt an ob das Gebet noch offen ist.
     */
    public boolean isOpen() {
        return status == PrayerStatus.OFFEN;
    }

    // ── Abstrakte Methoden implementiert ─────────────────────────────────────

    @Override
    public String formatContent() {
        return String.format("🙏 %s [%s] – %s (Gebete: %d)",
                title, status.name(), prayerText, prayerCount);
    }

    @Override
    public String toCsvString() {
        return String.format("PRAYER;%d;%s;%s;%b;%s;%s;%s;%s;%d",
                getId(), getAuthor(), getDateString(), isVisible(),
                getCategory(), title,
                prayerText.replace(";", ","),
                status.name(), prayerCount);
    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && title != null && !title.isBlank()
                && prayerText != null && !prayerText.isBlank()
                && status != null;
    }

    @Override
    public String toString() {
        return String.format("%s | %s [%s]", super.toString(), title, status.name());
    }
}
