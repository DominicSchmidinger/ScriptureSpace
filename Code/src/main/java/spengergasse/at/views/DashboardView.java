package spengergasse.at.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import spengergasse.at.model.*;
import spengergasse.at.service.ScriptureManager;

import java.util.Map;
import java.util.Optional;

/**
 * Dashboard-View: Statistiken, Tagesvers, Schnellübersicht.
 */
@PageTitle("Dashboard – Scripture Space")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final ScriptureManager manager;

    public DashboardView(ScriptureManager manager) {
        this.manager = manager;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("📊 Dashboard"));
        buildDailyVerse();
        buildStatCards();
        buildTopStats();
        buildActivitySummary();
    }

    private void buildDailyVerse() {
        Optional<VerseEntry> daily = manager.getDailyVerse();
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("padding", "1rem")
                .set("border-radius", "8px")
                .set("margin-bottom", "1rem");

        if (daily.isPresent()) {
            VerseEntry v = daily.get();
            card.add(new H3("📖 Tagesvers"));
            card.add(new Paragraph("\"" + v.getVerse() + "\""));
            card.add(new Span("– " + v.getReference()));
        } else {
            card.add(new Paragraph("Noch keine Verse vorhanden. Füge den ersten hinzu!"));
        }

        Button refresh = new Button("Anderen Vers", e -> {
            getUI().ifPresent(ui -> ui.navigate(DashboardView.class));
        });
        card.add(new Hr(), refresh);
        add(card);
    }

    private void buildStatCards() {
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);

        cards.add(
                statCard("📖 Verse",       manager.getVerseCount()),
                statCard("🙏 Gebete",      manager.getPrayerCount()),
                statCard("💬 Diskussionen",manager.getDiscussionCount()),
                statCard("✨ Zeugnisse",   manager.getTestimonyCount())
        );
        add(cards);
    }

    private Div statCard(String label, int count) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "1rem 1.5rem")
                .set("border-radius", "8px")
                .set("text-align", "center")
                .set("flex", "1");
        card.add(new H3(String.valueOf(count)));
        card.add(new Span(label));
        return card;
    }

    private void buildTopStats() {
        add(new H3("🏆 Auswertungen"));

        VerticalLayout stats = new VerticalLayout();
        stats.setPadding(false);

        // Meistaktiver Autor
        manager.getMostActiveAuthor().ifPresent(author ->
                stats.add(new Paragraph("👤 Aktivster Autor: " + author)));

        // Meistgebetetes Gebet
        manager.getMostPrayedPrayer().ifPresent(p ->
                stats.add(new Paragraph("🙏 Meistgebetetes Gebet: \"" + p.getTitle()
                        + "\" (" + p.getPrayerCount() + " Mitbetende)")));

        // Meistgeliktes Zeugnis
        manager.getMostLikedTestimony().ifPresent(t ->
                stats.add(new Paragraph("✨ Beliebtestes Zeugnis: \"" + t.getTitle()
                        + "\" (" + t.getLikeCount() + " Likes)")));

        // Erhörte Gebete
        stats.add(new Paragraph("✅ Erhörte Gebete: " + manager.getAnsweredPrayerCount()));

        // Heute-Einträge
        stats.add(new Paragraph("📅 Heute erstellt: " + manager.getTodayEntries().size()));

        if (stats.getComponentCount() == 0) {
            stats.add(new Paragraph("Noch keine Auswertungen verfügbar."));
        }

        add(stats);
    }

    private void buildActivitySummary() {
        add(new H3("📋 Einträge nach Kategorie"));

        Map<String, java.util.List<ContentEntry>> byCategory = manager.groupByCategory();
        if (byCategory.isEmpty()) {
            add(new Paragraph("Noch keine Einträge vorhanden."));
            return;
        }

        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        byCategory.forEach((cat, entries) ->
                list.add(new Paragraph("• " + cat + ": " + entries.size() + " Einträge")));
        add(list);
    }
}
