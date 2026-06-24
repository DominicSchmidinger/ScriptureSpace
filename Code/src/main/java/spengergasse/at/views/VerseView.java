package spengergasse.at.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import spengergasse.at.model.VerseEntry;
import spengergasse.at.service.ScriptureManager;
import spengergasse.at.util.InvalidInputException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View für Bibelverse: Grid, Suche, Filter, Hinzufügen, Bearbeiten, Löschen.
 */
@PageTitle("Bibelverse – Scripture Space")
@Route(value = "verses", layout = MainLayout.class)
public class VerseView extends VerticalLayout {

    private final ScriptureManager manager;
    private final Grid<VerseEntry> grid = new Grid<>(VerseEntry.class, false);

    private final TextField searchField   = new TextField();
    private final TextField bookFilter    = new TextField();
    private final Checkbox  visibleFilter = new Checkbox("Nur sichtbare");

    public VerseView(ScriptureManager manager) {
        this.manager = manager;
        setSizeFull();
        setPadding(true);

        add(new H2("📖 Bibelverse"));
        buildToolbar();
        buildGrid();
        refreshGrid();
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private void buildToolbar() {
        searchField.setPlaceholder("Suche...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshGrid());

        bookFilter.setPlaceholder("Buch filtern...");
        bookFilter.addValueChangeListener(e -> refreshGrid());

        visibleFilter.addValueChangeListener(e -> refreshGrid());

        Button addBtn = new Button("Neuer Vers", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openForm(null));

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, bookFilter, visibleFilter, addBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        add(toolbar);
    }

    // ── Grid ──────────────────────────────────────────────────────────────────

    private void buildGrid() {
        grid.addColumn(VerseEntry::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(VerseEntry::getReference).setHeader("Stelle").setSortable(true);
        grid.addColumn(VerseEntry::getVerse).setHeader("Vers").setFlexGrow(2);
        grid.addColumn(VerseEntry::getAuthor).setHeader("Autor").setSortable(true);
        grid.addColumn(VerseEntry::getCategory).setHeader("Kategorie").setSortable(true);
        grid.addColumn(v -> v.isVisible() ? "✅" : "❌").setHeader("Sichtbar");
        grid.addColumn(v -> v.isFavorite() ? "⭐" : "").setHeader("Fav");
        grid.addColumn(VerseEntry::getDateString).setHeader("Datum").setSortable(true);

        // Aktionen-Spalte
        grid.addComponentColumn(verse -> {
            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            edit.addClickListener(e -> openForm(verse));

            Button fav = new Button(verse.isFavorite() ? "★" : "☆");
            fav.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            fav.addClickListener(e -> {
                manager.toggleFavorite(verse.getId());
                refreshGrid();
            });

            Button del = new Button(VaadinIcon.TRASH.create());
            del.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_SMALL);
            del.addClickListener(e -> {
                manager.removeById(verse.getId());
                showSuccess("Vers gelöscht");
                refreshGrid();
            });

            return new HorizontalLayout(edit, fav, del);
        }).setHeader("Aktionen").setWidth("160px").setFlexGrow(0);

        grid.setSizeFull();
        add(grid);
    }

    // ── Formular-Dialog ───────────────────────────────────────────────────────

    private void openForm(VerseEntry existing) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeaderTitle(existing == null ? "Neuer Vers" : "Vers bearbeiten");

        TextField authorField   = new TextField("Autor");
        TextField bookField     = new TextField("Buch");
        IntegerField chapterField = new IntegerField("Kapitel");
        IntegerField verseNumField = new IntegerField("Versnummer");
        TextArea verseField     = new TextArea("Verstext");
        TextField categoryField = new TextField("Kategorie");
        Checkbox visibleCheck   = new Checkbox("Sichtbar");

        chapterField.setMin(1);
        verseNumField.setMin(1);
        verseField.setMinHeight("100px");

        if (existing != null) {
            authorField.setValue(existing.getAuthor());
            bookField.setValue(existing.getBook());
            chapterField.setValue(existing.getChapter());
            verseNumField.setValue(existing.getVerseNumber());
            verseField.setValue(existing.getVerse());
            categoryField.setValue(existing.getCategory());
            visibleCheck.setValue(existing.isVisible());
        } else {
            visibleCheck.setValue(true);
            categoryField.setValue("Allgemein");
        }

        FormLayout form = new FormLayout(
                authorField, categoryField, bookField,
                chapterField, verseNumField, verseField, visibleCheck);
        form.setColspan(verseField, 2);
        form.setColspan(visibleCheck, 2);

        Button save = new Button("Speichern", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                String today = LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                if (existing == null) {
                    manager.addVerse(
                            authorField.getValue(), today,
                            visibleCheck.getValue(),
                            categoryField.getValue(),
                            bookField.getValue(),
                            chapterField.getValue() != null ? chapterField.getValue() : 0,
                            verseNumField.getValue() != null ? verseNumField.getValue() : 0,
                            verseField.getValue());
                    showSuccess("Vers gespeichert!");
                } else {
                    existing.setAuthor(authorField.getValue());
                    existing.setBook(bookField.getValue());
                    existing.setChapter(chapterField.getValue() != null ? chapterField.getValue() : 0);
                    existing.setVerseNumber(verseNumField.getValue() != null ? verseNumField.getValue() : 0);
                    existing.setVerse(verseField.getValue());
                    existing.setCategory(categoryField.getValue());
                    existing.setVisible(visibleCheck.getValue());
                    showSuccess("Vers aktualisiert!");
                }
                dialog.close();
                refreshGrid();
            } catch (InvalidInputException ex) {
                showError(ex.getMessage());
            }
        });

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(form);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    private void refreshGrid() {
        List<VerseEntry> all = manager.getAllVerses()
                .stream()
                .filter(v -> {
                    String kw = searchField.getValue().toLowerCase().trim();
                    if (!kw.isBlank() && !v.formatContent().toLowerCase().contains(kw)
                            && !v.getAuthor().toLowerCase().contains(kw)) return false;

                    String book = bookFilter.getValue().toLowerCase().trim();
                    if (!book.isBlank() && !v.getBook().toLowerCase().contains(book)) return false;

                    if (visibleFilter.getValue() && !v.isVisible()) return false;
                    return true;
                })
                .toList();
        grid.setItems(all);
    }

    private void showSuccess(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification n = Notification.show("❌ " + msg, 5000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
