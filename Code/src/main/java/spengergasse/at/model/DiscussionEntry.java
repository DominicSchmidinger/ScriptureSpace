package spengergasse.at.model;

import spengergasse.at.util.InvalidInputException;

/**
 * Subklasse für Diskussionsfragen.
 * Kategorisiert nach Tiefe: ESSENTIAL, CASUAL oder DEEP_FAITH.
 */
public class DiscussionEntry extends ContentEntry {

    public enum DiscussionType { ESSENTIAL, CASUAL, DEEP_FAITH }

    private String question;
    private String context;      // optionaler Bibelkontext zur Frage
    private DiscussionType type;
    private int responseCount;   // Anzahl der Antworten

    // ── Konstruktor ──────────────────────────────────────────────────────────

    public DiscussionEntry(int id, String author, String date, boolean visible,
                           String category, String question, String context, DiscussionType type)
            throws InvalidInputException {
        super(id, author, date, visible, category);
        setQuestion(question);
        setContext(context);
        setType(type);
        this.responseCount = 0;
    }

    // ── Setter mit Validierung ────────────────────────────────────────────────

    public void setQuestion(String question) throws InvalidInputException {
        if (question == null || question.isBlank())
            throw new InvalidInputException("Frage darf nicht leer sein");
        if (question.length() > 500)
            throw new InvalidInputException("Frage darf max. 500 Zeichen haben");
        this.question = question.trim();
    }

    public void setContext(String context) {
        // Kontext ist optional
        this.context = (context == null || context.isBlank()) ? "" : context.trim();
    }

    public void setType(DiscussionType type) throws InvalidInputException {
        if (type == null) throw new InvalidInputException("Diskussionstyp darf nicht null sein");
        this.type = type;
    }

    public void setResponseCount(int responseCount) throws InvalidInputException {
        if (responseCount < 0)
            throw new InvalidInputException("Antwortanzahl darf nicht negativ sein");
        this.responseCount = responseCount;
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public String getQuestion()       { return question; }
    public String getContext()        { return context; }
    public DiscussionType getType()   { return type; }
    public int getResponseCount()     { return responseCount; }

    // ── Eigene Methoden ───────────────────────────────────────────────────────

    /**
     * Erhöht den Antworten-Zähler.
     */
    public void addResponse() {
        this.responseCount++;
    }

    /**
     * Gibt die Bezeichnung des Typs auf Deutsch zurück.
     */
    public String getTypeLabel() {
        return switch (type) {
            case ESSENTIAL  -> "Wesentliche Frage";
            case CASUAL     -> "Lockere Diskussion";
            case DEEP_FAITH -> "Tiefe Glaubensfrage";
        };
    }

    /**
     * Gibt an ob die Frage besonders tiefgründig ist.
     */
    public boolean isDeepQuestion() {
        return type == DiscussionType.DEEP_FAITH;
    }

    // ── Abstrakte Methoden implementiert ─────────────────────────────────────

    @Override
    public String formatContent() {
        String ctx = context.isBlank() ? "" : " [" + context + "]";
        return String.format("💬 [%s]%s %s (Antworten: %d)",
                getTypeLabel(), ctx, question, responseCount);
    }

    @Override
    public String toCsvString() {
        return String.format("DISCUSSION;%d;%s;%s;%b;%s;%s;%s;%s;%d",
                getId(), getAuthor(), getDateString(), isVisible(),
                getCategory(),
                question.replace(";", ","),
                context.replace(";", ","),
                type.name(), responseCount);
    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && question != null && !question.isBlank()
                && type != null;
    }

    @Override
    public String toString() {
        return String.format("%s | [%s] %s", super.toString(), type.name(), question);
    }
}
