package spengergasse.at.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import spengergasse.at.model.TestimonyEntry;
import spengergasse.at.service.ScriptureManager;
import spengergasse.at.util.InvalidInputException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Zeugnisse – Scripture Space")
@Route(value = "testimonies", layout = MainLayout.class)
public class TestimonyView extends VerticalLayout {

    private final ScriptureManager manager;
    private final Grid<TestimonyEntry> grid = new Grid<>(TestimonyEntry.class, false);

    private final TextField searchField  = new TextField();
    private final Checkbox  activeFilter = new Checkbox("Nur aktive (24h)");

    public TestimonyView(ScriptureManager manager) {
        this.manager = manager;
        setSizeFull();
        setPadding(true);

        add(new H2("✨ Glaubenszeugnisse"));
        add(new Paragraph("Teile deine Erfahrungen mit Gott mit der Gemeinschaft."));
        buildToolbar();
        buildGrid();
        refreshGrid();
    }

    private void buildToolbar() {
        searchField.setPlaceholder("Suche...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshGrid());

        activeFilter.addValueChangeListener(e -> refreshGrid());

        Button addBtn = new Button("Neues Zeugnis", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openForm(null));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, activeFilter, addBtn);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();
        add(toolbar);
    }

    private void buildGrid() {
        grid.addColumn(TestimonyEntry::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(TestimonyEntry::getTitle).setHeader("Titel").setWidth("160px").setFlexGrow(0);
        grid.addColumn(TestimonyEntry::getStory).setHeader("Zeugnis").setWidth("200px").setFlexGrow(0);
        grid.addColumn(TestimonyEntry::getAuthor).setHeader("Autor").setWidth("110px").setFlexGrow(0);
        grid.addColumn(t -> t.isActive() ? "✅ Aktiv" : "⏰ Abgelaufen").setHeader("Status").setWidth("120px").setFlexGrow(0);
        grid.addColumn(TestimonyEntry::getLikeCount).setHeader("❤️ Likes").setWidth("90px").setFlexGrow(0);
        grid.addColumn(TestimonyEntry::getDateString).setHeader("Datum").setWidth("110px").setFlexGrow(0);

        grid.addComponentColumn(t -> {
            Button like = new Button("❤️ " + t.getLikeCount());
            like.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            like.addClickListener(e -> {
                t.addLike();
                showSuccess("Like hinzugefügt! ❤️");
                refreshGrid();
            });

            Button expire = new Button("⏰");
            expire.getElement().setAttribute("title", "Ablaufen lassen");
            expire.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            expire.setEnabled(!t.isExpired());
            expire.addClickListener(e -> {
                t.expire();
                showSuccess("Zeugnis als abgelaufen markiert");
                refreshGrid();
            });

            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            edit.addClickListener(e -> openForm(t));

            Button del = new Button(VaadinIcon.TRASH.create());
            del.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            del.addClickListener(e -> {
                manager.removeById(t.getId());
                showSuccess("Zeugnis gelöscht");
                refreshGrid();
            });

            HorizontalLayout actions = new HorizontalLayout(like, expire, edit, del);
            actions.setSpacing(false);
            return actions;
        }).setHeader("Aktionen").setWidth("220px").setFlexGrow(0);

        grid.setMinWidth("1070px");
        grid.setSizeFull();

        Div wrapper = new Div(grid);
        wrapper.getStyle().set("overflow-x", "auto");
        wrapper.setSizeFull();
        add(wrapper);
    }

    private void openForm(TestimonyEntry existing) {
        Dialog dialog = new Dialog();
        dialog.setWidth("580px");
        dialog.setHeaderTitle(existing == null ? "Neues Zeugnis" : "Zeugnis bearbeiten");

        TextField titleField    = new TextField("Titel");
        TextArea  storyField    = new TextArea("Dein Zeugnis");
        TextField authorField   = new TextField("Autor");
        TextField categoryField = new TextField("Kategorie");
        Checkbox  visibleCheck  = new Checkbox("Sichtbar");

        storyField.setMinHeight("150px");
        storyField.setPlaceholder("Was hat Gott in deinem Leben getan?");

        if (existing != null) {
            titleField.setValue(existing.getTitle());
            storyField.setValue(existing.getStory());
            authorField.setValue(existing.getAuthor());
            categoryField.setValue(existing.getCategory());
            visibleCheck.setValue(existing.isVisible());
        } else {
            visibleCheck.setValue(true);
            categoryField.setValue("Zeugnis");
        }

        FormLayout form = new FormLayout(titleField, authorField, categoryField, visibleCheck, storyField);
        form.setColspan(storyField, 2);
        form.setColspan(visibleCheck, 2);

        Button save = new Button("Speichern", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                if (existing == null) {
                    manager.addTestimony(authorField.getValue(), today, visibleCheck.getValue(),
                            categoryField.getValue(), titleField.getValue(), storyField.getValue());
                    showSuccess("Zeugnis geteilt! 🙏");
                } else {
                    existing.setTitle(titleField.getValue());
                    existing.setStory(storyField.getValue());
                    existing.setAuthor(authorField.getValue());
                    existing.setCategory(categoryField.getValue());
                    existing.setVisible(visibleCheck.getValue());
                    showSuccess("Zeugnis aktualisiert!");
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

    private void refreshGrid() {
        List<TestimonyEntry> all = manager.getAllTestimonies().stream()
                .filter(t -> {
                    String kw = searchField.getValue().toLowerCase().trim();
                    if (!kw.isBlank() && !t.getTitle().toLowerCase().contains(kw)
                            && !t.getAuthor().toLowerCase().contains(kw)) return false;
                    if (activeFilter.getValue() && !t.isActive()) return false;
                    return true;
                }).toList();
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
