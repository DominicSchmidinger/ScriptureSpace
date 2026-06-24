package spengergasse.at;

import org.junit.jupiter.api.*;
import spengergasse.at.model.*;
import spengergasse.at.service.ScriptureManager;
import spengergasse.at.util.InvalidInputException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-Testklasse für ScriptureManager.
 * Testet Hinzufügen, Entfernen, Suchen, Filtern, Sortieren und Auswertungen.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScriptureManagerTest {

    private ScriptureManager manager;

    @BeforeEach
    void setUp() {
        manager = new ScriptureManager();
        // Testdaten einfügen
        manager.addVerse("Maria", "01.01.2025", true, "NT",
                "Johannes", 3, 16,
                "Denn so sehr hat Gott die Welt geliebt...");
        manager.addVerse("Peter", "15.03.2025", true, "AT",
                "Psalm", 23, 1,
                "Der Herr ist mein Hirte, mir wird nichts mangeln.");
        manager.addVerse("Anna", "20.06.2025", false, "NT",
                "Römer", 8, 28,
                "Wir wissen aber, dass denen, die Gott lieben, alle Dinge zum Besten dienen.");

        manager.addPrayer("Maria", "01.01.2025", true, "Gebet",
                "Heilung für meine Mutter", "Bitte bete für die Gesundheit meiner Mutter.");
        manager.addPrayer("Thomas", "10.02.2025", true, "Gebet",
                "Weisheit für Entscheidung", "Ich brauche Gottes Führung bei meiner Berufswahl.");

        manager.addDiscussion("Lukas", "05.04.2025", true, "Diskussion",
                "Was bedeutet Glaube ohne Werke?", "Jakobus 2:17",
                DiscussionEntry.DiscussionType.DEEP_FAITH);
        manager.addDiscussion("Sarah", "12.05.2025", true, "Diskussion",
                "Wie war eure Woche mit Gott?", "",
                DiscussionEntry.DiscussionType.CASUAL);

        manager.addTestimony("Johannes", "01.06.2025", true, "Zeugnis",
                "Gott hat mein Gebet erhört",
                "Nach langer Zeit der Stille hat Gott geantwortet...");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HINZUFÜGEN
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    void testAddVerse_valid() {
        int before = manager.getVerseCount();
        manager.addVerse("Test", "01.01.2024", true, "Kat", "Genesis", 1, 1, "Am Anfang...");
        assertEquals(before + 1, manager.getVerseCount());
    }

    @Test
    @Order(2)
    void testAddVerse_invalidAuthor_throwsException() {
        assertThrows(InvalidInputException.class, () ->
                manager.addVerse("", "01.01.2024", true, "Kat", "Genesis", 1, 1, "Text"));
    }

    @Test
    @Order(3)
    void testAddVerse_invalidChapter_throwsException() {
        assertThrows(InvalidInputException.class, () ->
                manager.addVerse("Test", "01.01.2024", true, "Kat", "Genesis", 0, 1, "Text"));
    }

    @Test
    @Order(4)
    void testAddVerse_invalidDate_throwsException() {
        assertThrows(InvalidInputException.class, () ->
                manager.addVerse("Test", "2024-01-01", true, "Kat", "Genesis", 1, 1, "Text"));
    }

    @Test
    @Order(5)
    void testAddPrayer_valid() {
        int before = manager.getPrayerCount();
        manager.addPrayer("Test", "01.01.2024", true, "Kat", "Titel", "Gebetstext");
        assertEquals(before + 1, manager.getPrayerCount());
    }

    @Test
    @Order(6)
    void testAddDiscussion_nullType_throwsException() {
        assertThrows(InvalidInputException.class, () ->
                manager.addDiscussion("Test", "01.01.2024", true, "Kat", "Frage?", "", null));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ENTFERNEN
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(7)
    void testRemoveById_exists() {
        int id = manager.getAllVerses().get(0).getId();
        int before = manager.getTotalCount();
        assertTrue(manager.removeById(id));
        assertEquals(before - 1, manager.getTotalCount());
    }

    @Test
    @Order(8)
    void testRemoveById_notExists() {
        assertFalse(manager.removeById(9999));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SUCHE
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(9)
    void testFindById_found() {
        int id = manager.getAllVerses().get(0).getId();
        Optional<ContentEntry> result = manager.findById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @Order(10)
    void testFindById_notFound() {
        Optional<ContentEntry> result = manager.findById(9999);
        assertFalse(result.isPresent());
    }

    @Test
    @Order(11)
    void testSearchAll_byKeyword() {
        List<ContentEntry> results = manager.searchAll("Johannes");
        assertFalse(results.isEmpty());
    }

    @Test
    @Order(12)
    void testSearchAll_emptyKeyword_returnsAll() {
        List<ContentEntry> results = manager.searchAll("");
        assertEquals(manager.getTotalCount(), results.size());
    }

    @Test
    @Order(13)
    void testSearchVersesByBook() {
        List<VerseEntry> results = manager.searchVersesByBook("Johannes");
        assertEquals(1, results.size());
        assertEquals("Johannes", results.get(0).getBook());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FILTER
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(14)
    void testFilterByCategory() {
        List<ContentEntry> result = manager.filterByCategory("NT");
        assertEquals(2, result.size()); // Johannes + Römer (Anna ist nicht sichtbar aber gefiltert nach Kat)
    }

    @Test
    @Order(15)
    void testFilterVisible() {
        List<ContentEntry> result = manager.filterVisible();
        assertTrue(result.stream().allMatch(ContentEntry::isVisible));
    }

    @Test
    @Order(16)
    void testFilterDiscussionsByType() {
        List<DiscussionEntry> deep = manager.filterDiscussionsByType(
                DiscussionEntry.DiscussionType.DEEP_FAITH);
        assertEquals(1, deep.size());
        assertTrue(deep.get(0).isDeepQuestion());
    }

    @Test
    @Order(17)
    void testFilterCombined() {
        List<ContentEntry> result = manager.filterCombined("NT", null, true);
        assertTrue(result.stream().allMatch(e -> e.getCategory().equals("NT")));
        assertTrue(result.stream().allMatch(ContentEntry::isVisible));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SORTIERUNG
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(18)
    void testSortByDateAscending() {
        List<ContentEntry> sorted = manager.sortByDate(true);
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertFalse(sorted.get(i).getDate().isAfter(sorted.get(i + 1).getDate()));
        }
    }

    @Test
    @Order(19)
    void testSortByDateDescending() {
        List<ContentEntry> sorted = manager.sortByDate(false);
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertFalse(sorted.get(i).getDate().isBefore(sorted.get(i + 1).getDate()));
        }
    }

    @Test
    @Order(20)
    void testSortByAuthorAscending() {
        List<ContentEntry> sorted = manager.sortByAuthor(true);
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).getAuthor()
                    .compareToIgnoreCase(sorted.get(i + 1).getAuthor()) <= 0);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GRUPPIERUNG
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(21)
    void testGroupByType() {
        Map<String, List<ContentEntry>> groups = manager.groupByType();
        assertTrue(groups.containsKey("VerseEntry"));
        assertTrue(groups.containsKey("PrayerEntry"));
    }

    @Test
    @Order(22)
    void testGroupByAuthor() {
        Map<String, List<ContentEntry>> groups = manager.groupByAuthor();
        assertTrue(groups.containsKey("Maria"));
        assertEquals(2, groups.get("Maria").size()); // 1 Vers + 1 Gebet
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AUSWERTUNGEN
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(23)
    void testGetTotalCount() {
        assertEquals(8, manager.getTotalCount());
    }

    @Test
    @Order(24)
    void testGetAveragePrayerCount_initial() {
        // Beide Gebete haben 0 Mitbetende
        assertEquals(0.0, manager.getAveragePrayerCount());
    }

    @Test
    @Order(25)
    void testGetAveragePrayerCount_afterPraying() {
        manager.getAllPrayers().get(0).addPrayer();
        manager.getAllPrayers().get(0).addPrayer();
        // 2 + 0 / 2 = 1.0
        assertEquals(1.0, manager.getAveragePrayerCount());
    }

    @Test
    @Order(26)
    void testGetAnsweredPrayerCount() {
        assertEquals(0, manager.getAnsweredPrayerCount());
        manager.getAllPrayers().get(0).markAsAnswered();
        assertEquals(1, manager.getAnsweredPrayerCount());
    }

    @Test
    @Order(27)
    void testGetMostActiveAuthor() {
        Optional<String> top = manager.getMostActiveAuthor();
        assertTrue(top.isPresent());
        assertEquals("Maria", top.get()); // Maria hat 2 Einträge
    }

    @Test
    @Order(28)
    void testGetDailyVerse_notEmpty() {
        Optional<VerseEntry> daily = manager.getDailyVerse();
        assertTrue(daily.isPresent());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FAVORITEN
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(29)
    void testToggleFavorite() {
        int id = manager.getAllVerses().get(0).getId();
        assertFalse(manager.findById(id).get().isFavorite());
        manager.toggleFavorite(id);
        assertTrue(manager.findById(id).get().isFavorite());
        manager.toggleFavorite(id);
        assertFalse(manager.findById(id).get().isFavorite());
    }

    @Test
    @Order(30)
    void testToggleFavorite_invalidId() {
        assertThrows(InvalidInputException.class, () -> manager.toggleFavorite(9999));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  VERLAUFSLOG
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(31)
    void testActivityLog_notEmpty() {
        assertFalse(manager.getActivityLog().isEmpty());
    }

    @Test
    @Order(32)
    void testClearLog() {
        manager.clearLog();
        assertTrue(manager.getActivityLog().isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODELL-TESTS
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(33)
    void testVerseEntry_isNewTestament() {
        VerseEntry v = new VerseEntry(100, "Test", "01.01.2024", true, "NT",
                "Johannes", 3, 16, "...");
        assertTrue(v.isNewTestament());
    }

    @Test
    @Order(34)
    void testVerseEntry_getReference() {
        VerseEntry v = new VerseEntry(101, "Test", "01.01.2024", true, "NT",
                "Psalm", 23, 1, "...");
        assertEquals("Psalm 23:1", v.getReference());
    }

    @Test
    @Order(35)
    void testPrayerEntry_markAsAnswered() {
        PrayerEntry p = new PrayerEntry(200, "Test", "01.01.2024", true, "Gebet",
                "Titel", "Text");
        assertTrue(p.isOpen());
        p.markAsAnswered();
        assertEquals(PrayerEntry.PrayerStatus.ERHÖRT, p.getStatus());
        assertFalse(p.isOpen());
    }

    @Test
    @Order(36)
    void testDiscussionEntry_isDeepQuestion() {
        DiscussionEntry d = new DiscussionEntry(300, "Test", "01.01.2024", true, "Kat",
                "Was ist Glaube?", "", DiscussionEntry.DiscussionType.DEEP_FAITH);
        assertTrue(d.isDeepQuestion());
    }

    @Test
    @Order(37)
    void testTestimonyEntry_isActive() {
        // Heutiges Datum → aktiv
        String today = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        TestimonyEntry t = new TestimonyEntry(400, "Test", today, true, "Zeugnis",
                "Titel", "Geschichte");
        assertTrue(t.isActive());
        t.expire();
        assertFalse(t.isActive());
    }

    @Test
    @Order(38)
    void testContentEntry_isValid() {
        VerseEntry v = manager.getAllVerses().get(0);
        assertTrue(v.isValid());
    }
}
