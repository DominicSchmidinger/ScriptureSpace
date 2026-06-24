package spengergasse.at.service;

import spengergasse.at.model.*;
import spengergasse.at.util.InvalidInputException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Verwaltungsklasse für Scripture Space.
 * Verwaltet alle Einträge, bietet Fachlogik, Suche, Filter und Auswertungen.
 */
public class ScriptureManager {

    // ── Listen ────────────────────────────────────────────────────────────────

    private final List<VerseEntry>      verses      = new ArrayList<>();
    private final List<PrayerEntry>     prayers     = new ArrayList<>();
    private final List<DiscussionEntry> discussions = new ArrayList<>();
    private final List<TestimonyEntry>  testimonies = new ArrayList<>();
    private final List<String>          activityLog = new ArrayList<>(); // Verlaufsfunktion

    // Map für schnellen ID-Zugriff über alle Typen
    private final Map<Integer, ContentEntry> allEntriesMap = new HashMap<>();

    private int nextId = 1;

    // ── ID Verwaltung ─────────────────────────────────────────────────────────

    private int getNextId() { return nextId++; }

    public void setNextId(int id) { if (id > nextId) this.nextId = id; }

    // ══════════════════════════════════════════════════════════════════════════
    //  HINZUFÜGEN
    // ══════════════════════════════════════════════════════════════════════════

    public VerseEntry addVerse(String author, String date, boolean visible,
                               String category, String book, int chapter,
                               int verseNumber, String verse) {
        try {
            VerseEntry e = new VerseEntry(getNextId(), author, date, visible,
                    category, book, chapter, verseNumber, verse);
            verses.add(e);
            allEntriesMap.put(e.getId(), e);
            log("Vers hinzugefügt: " + e.getReference() + " von " + author);
            return e;
        } catch (InvalidInputException ex) {
            throw new InvalidInputException("Fehler beim Hinzufügen des Verses: " + ex.getMessage(), ex);
        }
    }

    public PrayerEntry addPrayer(String author, String date, boolean visible,
                                 String category, String title, String prayerText) {
        try {
            PrayerEntry e = new PrayerEntry(getNextId(), author, date, visible,
                    category, title, prayerText);
            prayers.add(e);
            allEntriesMap.put(e.getId(), e);
            log("Gebet hinzugefügt: \"" + title + "\" von " + author);
            return e;
        } catch (InvalidInputException ex) {
            throw new InvalidInputException("Fehler beim Hinzufügen des Gebets: " + ex.getMessage(), ex);
        }
    }

    public DiscussionEntry addDiscussion(String author, String date, boolean visible,
                                         String category, String question,
                                         String context, DiscussionEntry.DiscussionType type) {
        try {
            DiscussionEntry e = new DiscussionEntry(getNextId(), author, date, visible,
                    category, question, context, type);
            discussions.add(e);
            allEntriesMap.put(e.getId(), e);
            log("Diskussion hinzugefügt: [" + type.name() + "] von " + author);
            return e;
        } catch (InvalidInputException ex) {
            throw new InvalidInputException("Fehler beim Hinzufügen der Diskussion: " + ex.getMessage(), ex);
        }
    }

    public TestimonyEntry addTestimony(String author, String date, boolean visible,
                                       String category, String title, String story) {
        try {
            TestimonyEntry e = new TestimonyEntry(getNextId(), author, date, visible,
                    category, title, story);
            testimonies.add(e);
            allEntriesMap.put(e.getId(), e);
            log("Zeugnis hinzugefügt: \"" + title + "\" von " + author);
            return e;
        } catch (InvalidInputException ex) {
            throw new InvalidInputException("Fehler beim Hinzufügen des Zeugnisses: " + ex.getMessage(), ex);
        }
    }

    // ── Direkt-Objekt hinzufügen (für CSV-Import) ─────────────────────────────

    public void addVerseDirectly(VerseEntry e) {
        verses.add(e);
        allEntriesMap.put(e.getId(), e);
        if (e.getId() >= nextId) nextId = e.getId() + 1;
    }

    public void addPrayerDirectly(PrayerEntry e) {
        prayers.add(e);
        allEntriesMap.put(e.getId(), e);
        if (e.getId() >= nextId) nextId = e.getId() + 1;
    }

    public void addDiscussionDirectly(DiscussionEntry e) {
        discussions.add(e);
        allEntriesMap.put(e.getId(), e);
        if (e.getId() >= nextId) nextId = e.getId() + 1;
    }

    public void addTestimonyDirectly(TestimonyEntry e) {
        testimonies.add(e);
        allEntriesMap.put(e.getId(), e);
        if (e.getId() >= nextId) nextId = e.getId() + 1;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ENTFERNEN
    // ══════════════════════════════════════════════════════════════════════════

    public boolean removeById(int id) {
        ContentEntry entry = allEntriesMap.remove(id);
        if (entry == null) return false;

        boolean removed = verses.removeIf(e -> e.getId() == id)
                || prayers.removeIf(e -> e.getId() == id)
                || discussions.removeIf(e -> e.getId() == id)
                || testimonies.removeIf(e -> e.getId() == id);

        if (removed) log("Eintrag gelöscht, ID=" + id);
        return removed;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SUCHE
    // ══════════════════════════════════════════════════════════════════════════

    public Optional<ContentEntry> findById(int id) {
        return Optional.ofNullable(allEntriesMap.get(id));
    }

    /** Sucht in allen Einträgen nach dem Suchbegriff (Autor, Kategorie, Inhalt). */
    public List<ContentEntry> searchAll(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllEntries();
        String kw = keyword.toLowerCase().trim();
        return getAllEntries().stream()
                .filter(e -> e.getAuthor().toLowerCase().contains(kw)
                        || e.getCategory().toLowerCase().contains(kw)
                        || e.formatContent().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public List<VerseEntry> searchVersesByBook(String book) {
        if (book == null || book.isBlank()) return new ArrayList<>(verses);
        String b = book.toLowerCase().trim();
        return verses.stream()
                .filter(v -> v.getBook().toLowerCase().contains(b))
                .collect(Collectors.toList());
    }

    public List<PrayerEntry> searchPrayersByStatus(PrayerEntry.PrayerStatus status) {
        return prayers.stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FILTER
    // ══════════════════════════════════════════════════════════════════════════

    public List<ContentEntry> filterByCategory(String category) {
        return getAllEntries().stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<ContentEntry> filterByAuthor(String author) {
        return getAllEntries().stream()
                .filter(e -> e.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    public List<ContentEntry> filterVisible() {
        return getAllEntries().stream()
                .filter(ContentEntry::isVisible)
                .collect(Collectors.toList());
    }

    public List<ContentEntry> filterFavorites() {
        return getAllEntries().stream()
                .filter(ContentEntry::isFavorite)
                .collect(Collectors.toList());
    }

    public List<DiscussionEntry> filterDiscussionsByType(DiscussionEntry.DiscussionType type) {
        return discussions.stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
    }

    public List<TestimonyEntry> filterActiveTestimonies() {
        return testimonies.stream()
                .filter(TestimonyEntry::isActive)
                .collect(Collectors.toList());
    }

    /** Kombinierter Filter: Kategorie + sichtbar + Typ */
    public List<ContentEntry> filterCombined(String category, String author, boolean onlyVisible) {
        return getAllEntries().stream()
                .filter(e -> category == null || category.isBlank()
                        || e.getCategory().equalsIgnoreCase(category))
                .filter(e -> author == null || author.isBlank()
                        || e.getAuthor().equalsIgnoreCase(author))
                .filter(e -> !onlyVisible || e.isVisible())
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SORTIERUNG
    // ══════════════════════════════════════════════════════════════════════════

    public List<ContentEntry> sortByDate(boolean ascending) {
        return getAllEntries().stream()
                .sorted(ascending
                        ? Comparator.comparing(ContentEntry::getDate)
                        : Comparator.comparing(ContentEntry::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<ContentEntry> sortByAuthor(boolean ascending) {
        return getAllEntries().stream()
                .sorted(ascending
                        ? Comparator.comparing(ContentEntry::getAuthor)
                        : Comparator.comparing(ContentEntry::getAuthor).reversed())
                .collect(Collectors.toList());
    }

    public List<PrayerEntry> sortPrayersByCount(boolean ascending) {
        return prayers.stream()
                .sorted(ascending
                        ? Comparator.comparingInt(PrayerEntry::getPrayerCount)
                        : Comparator.comparingInt(PrayerEntry::getPrayerCount).reversed())
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GRUPPIERUNG
    // ══════════════════════════════════════════════════════════════════════════

    /** Gruppiert alle Einträge nach Kategorie. */
    public Map<String, List<ContentEntry>> groupByCategory() {
        return getAllEntries().stream()
                .collect(Collectors.groupingBy(ContentEntry::getCategory));
    }

    /** Gruppiert alle Einträge nach Autor. */
    public Map<String, List<ContentEntry>> groupByAuthor() {
        return getAllEntries().stream()
                .collect(Collectors.groupingBy(ContentEntry::getAuthor));
    }

    /** Gruppiert nach Klasse (Typ). */
    public Map<String, List<ContentEntry>> groupByType() {
        return getAllEntries().stream()
                .collect(Collectors.groupingBy(e -> e.getClass().getSimpleName()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AUSWERTUNGEN / STATISTIKEN
    // ══════════════════════════════════════════════════════════════════════════

    public int getTotalCount()       { return allEntriesMap.size(); }
    public int getVerseCount()       { return verses.size(); }
    public int getPrayerCount()      { return prayers.size(); }
    public int getDiscussionCount()  { return discussions.size(); }
    public int getTestimonyCount()   { return testimonies.size(); }

    /** Durchschnittliche Anzahl an Mitbetenden pro Gebet. */
    public double getAveragePrayerCount() {
        return prayers.stream()
                .mapToInt(PrayerEntry::getPrayerCount)
                .average()
                .orElse(0.0);
    }

    /** Gebet mit den meisten Mitbetenden. */
    public Optional<PrayerEntry> getMostPrayedPrayer() {
        return prayers.stream()
                .max(Comparator.comparingInt(PrayerEntry::getPrayerCount));
    }

    /** Zeugnis mit den meisten Likes. */
    public Optional<TestimonyEntry> getMostLikedTestimony() {
        return testimonies.stream()
                .max(Comparator.comparingInt(TestimonyEntry::getLikeCount));
    }

    /** Anzahl der erhörten Gebete. */
    public long getAnsweredPrayerCount() {
        return prayers.stream()
                .filter(p -> p.getStatus() == PrayerEntry.PrayerStatus.ERHÖRT)
                .count();
    }

    /** Kombiniert: filtert nach Autor und zählt seine Einträge. */
    public Map<String, Long> getCountPerAuthor() {
        return getAllEntries().stream()
                .collect(Collectors.groupingBy(ContentEntry::getAuthor, Collectors.counting()));
    }

    /** Top-Autor nach Anzahl Einträge. */
    public Optional<String> getMostActiveAuthor() {
        return getCountPerAuthor().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /** Heute erstellte Einträge. */
    public List<ContentEntry> getTodayEntries() {
        return getAllEntries().stream()
                .filter(ContentEntry::isToday)
                .collect(Collectors.toList());
    }

    /** Zufälliger Tagesvers. */
    public Optional<VerseEntry> getDailyVerse() {
        if (verses.isEmpty()) return Optional.empty();
        Random rnd = new Random();
        return Optional.of(verses.get(rnd.nextInt(verses.size())));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ALLE EINTRÄGE
    // ══════════════════════════════════════════════════════════════════════════

    public List<ContentEntry> getAllEntries() {
        List<ContentEntry> all = new ArrayList<>();
        all.addAll(verses);
        all.addAll(prayers);
        all.addAll(discussions);
        all.addAll(testimonies);
        return all;
    }

    public List<VerseEntry>      getAllVerses()      { return Collections.unmodifiableList(verses); }
    public List<PrayerEntry>     getAllPrayers()     { return Collections.unmodifiableList(prayers); }
    public List<DiscussionEntry> getAllDiscussions() { return Collections.unmodifiableList(discussions); }
    public List<TestimonyEntry>  getAllTestimonies() { return Collections.unmodifiableList(testimonies); }

    // ══════════════════════════════════════════════════════════════════════════
    //  FAVORITEN-SYSTEM
    // ══════════════════════════════════════════════════════════════════════════

    public boolean toggleFavorite(int id) {
        ContentEntry e = allEntriesMap.get(id);
        if (e == null) throw new InvalidInputException("Eintrag mit ID " + id + " nicht gefunden");
        e.setFavorite(!e.isFavorite());
        log("Favorit geändert für ID=" + id + ": " + e.isFavorite());
        return e.isFavorite();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  VERLAUFSLOG
    // ══════════════════════════════════════════════════════════════════════════

    private void log(String message) {
        String entry = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                + " – " + message;
        activityLog.add(entry);
    }

    public List<String> getActivityLog() {
        return Collections.unmodifiableList(activityLog);
    }

    public void clearLog() { activityLog.clear(); }
}
