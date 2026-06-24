package spengergasse.at.service;

import spengergasse.at.model.*;
import spengergasse.at.util.InvalidInputException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/**
 * Kümmert sich um das Speichern und Laden aller Daten als CSV-Datei.
 * Format pro Zeile: TYP;id;author;date;visible;category;...felder...
 */
public class CsvService {

    private static final String DEFAULT_FILE = "scripture_data.csv";
    private final String filePath;

    public CsvService() {
        this.filePath = DEFAULT_FILE;
    }

    public CsvService(String filePath) {
        this.filePath = filePath;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SPEICHERN
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Speichert alle Einträge des Managers in die CSV-Datei.
     */
    public void saveAll(ScriptureManager manager) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), StandardCharsets.UTF_8)) {

            writer.write("# Scripture Space Data – bitte nicht manuell bearbeiten");
            writer.newLine();

            for (ContentEntry e : manager.getAllEntries()) {
                writer.write(e.toCsvString());
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new IOException("Fehler beim Speichern der Datei: " + filePath, ex);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LADEN
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Lädt alle Einträge aus der CSV-Datei in den Manager.
     * Gibt die Anzahl der geladenen Einträge zurück.
     */
    public int loadAll(ScriptureManager manager) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return 0;

        int count = 0;
        List<String> lines;

        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IOException("Fehler beim Lesen der Datei: " + filePath, ex);
        }

        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#")) continue;
            try {
                parseLine(line, manager);
                count++;
            } catch (Exception ex) {
                // fehlerhafte Zeile überspringen, aber weitermachen
                System.err.println("Zeile konnte nicht geladen werden: " + line);
                System.err.println("Ursache: " + ex.getMessage());
            }
        }
        return count;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PARSEN
    // ══════════════════════════════════════════════════════════════════════════

    private void parseLine(String line, ScriptureManager manager) {
        String[] parts = line.split(";", -1);
        if (parts.length < 6) throw new InvalidInputException("Zu wenige Felder: " + line);

        String type    = parts[0].trim();
        int    id      = Integer.parseInt(parts[1].trim());
        String author  = parts[2].trim();
        String date    = parts[3].trim();
        boolean visible = Boolean.parseBoolean(parts[4].trim());
        String category = parts[5].trim();

        switch (type) {
            case "VERSE" -> {
                // VERSE;id;author;date;visible;category;book;chapter;verseNumber;verse
                if (parts.length < 10) throw new InvalidInputException("Zu wenige Felder für VERSE");
                String book       = parts[6].trim();
                int chapter       = Integer.parseInt(parts[7].trim());
                int verseNumber   = Integer.parseInt(parts[8].trim());
                String verse      = parts[9].trim();
                VerseEntry e = new VerseEntry(id, author, date, visible, category,
                        book, chapter, verseNumber, verse);
                manager.addVerseDirectly(e);
            }
            case "PRAYER" -> {
                // PRAYER;id;author;date;visible;category;title;text;status;count
                if (parts.length < 10) throw new InvalidInputException("Zu wenige Felder für PRAYER");
                String title  = parts[6].trim();
                String text   = parts[7].trim();
                PrayerEntry.PrayerStatus status =
                        PrayerEntry.PrayerStatus.valueOf(parts[8].trim());
                int cnt = Integer.parseInt(parts[9].trim());
                PrayerEntry e = new PrayerEntry(id, author, date, visible, category, title, text);
                e.setStatus(status);
                e.setPrayerCount(cnt);
                manager.addPrayerDirectly(e);
            }
            case "DISCUSSION" -> {
                // DISCUSSION;id;author;date;visible;category;question;context;type;count
                if (parts.length < 10) throw new InvalidInputException("Zu wenige Felder für DISCUSSION");
                String question = parts[6].trim();
                String context  = parts[7].trim();
                DiscussionEntry.DiscussionType dtype =
                        DiscussionEntry.DiscussionType.valueOf(parts[8].trim());
                int cnt = Integer.parseInt(parts[9].trim());
                DiscussionEntry e = new DiscussionEntry(id, author, date, visible, category,
                        question, context, dtype);
                e.setResponseCount(cnt);
                manager.addDiscussionDirectly(e);
            }
            case "TESTIMONY" -> {
                // TESTIMONY;id;author;date;visible;category;title;story;expired;likes
                if (parts.length < 10) throw new InvalidInputException("Zu wenige Felder für TESTIMONY");
                String title   = parts[6].trim();
                String story   = parts[7].trim();
                boolean expired = Boolean.parseBoolean(parts[8].trim());
                int likes       = Integer.parseInt(parts[9].trim());
                TestimonyEntry e = new TestimonyEntry(id, author, date, visible, category, title, story);
                e.setExpired(expired);
                e.setLikeCount(likes);
                manager.addTestimonyDirectly(e);
            }
            default -> throw new InvalidInputException("Unbekannter Eintragstyp: " + type);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HILFS-METHODEN
    // ══════════════════════════════════════════════════════════════════════════

    public boolean fileExists() {
        return Files.exists(Paths.get(filePath));
    }

    public String getFilePath() { return filePath; }
}
