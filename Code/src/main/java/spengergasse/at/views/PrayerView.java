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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import spengergasse.at.model.PrayerEntry;
import spengergasse.at.service.ScriptureManager;
import spengergasse.at.util.InvalidInputException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Gebete – Scripture Space")
@Route(value = "prayers", layout = MainLayout.class)
public class PrayerView extends VerticalLayout {

    private final ScriptureManager manager;
    private final Grid<PrayerEntry> grid = new Grid<>(PrayerEntry.class, false);

    private final TextField searchField = new TextField();
    private final ComboBox<PrayerEntry.PrayerStatus> statusFilter = new ComboBox<>("Status");

    public PrayerView(ScriptureManager manager) {
        this.manager = manager;
        setSizeFull();
        setPadding(true);

        add(new H2("🙏 Gebete"));
        buildToolbar();
        buildGrid();
        refreshGrid();
    }

    private void buildToolbar() {
        searchField.setPlaceholder("Suche...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshGrid());

        statusFilter.setItems(PrayerEntry.PrayerStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> refreshGrid());

        Button addBtn = new Button("Neues Gebet", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openForm(null));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, statusFilter, addBtn);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();
        add(toolbar);
    }

    private void buildGrid() {
        grid.addColumn(PrayerEntry::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(PrayerEntry::getTitle).setHeader("Titel").setWidth("180px").setFlexGrow(0);
        grid.addColumn(PrayerEntry::getPrayerText).setHeader("Gebet").setWidth("220px").setFlexGrow(0);
        grid.addColumn(PrayerEntry::getAuthor).setHeader("Autor").setWidth("120px").setFlexGrow(0);
        grid.addColumn(p -> p.getStatus().name()).setHeader("Status").setWidth("100px").setFlexGrow(0);
        grid.addColumn(PrayerEntry::getPrayerCount).setHeader("Mitbetende").setWidth("110px").setFlexGrow(0);
        grid.addColumn(PrayerEntry::getDateString).setHeader("Datum").setWidth("110px").setFlexGrow(0);

        grid.addComponentColumn(prayer -> {
            Button prayWith = new Button("🙏 Beten");
            prayWith.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            prayWith.addClickListener(e -> {
                prayer.addPrayer();
                showSuccess("Danke, dass du mitbetest!");
                refreshGrid();
            });

            Button answered = new Button("✅ Erhört");
            answered.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            answered.setEnabled(prayer.isOpen());
            answered.addClickListener(e -> {
                prayer.markAsAnswered();
                showSuccess("Gebet als erhört markiert! 🎉");
                refreshGrid();
            });

            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            edit.addClickListener(e -> openForm(prayer));

            Button del = new Button(VaadinIcon.TRASH.create());
            del.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            del.addClickListener(e -> {
                manager.removeById(prayer.getId());
                showSuccess("Gebet gelöscht");
                refreshGrid();
            });

            HorizontalLayout actions = new HorizontalLayout(prayWith, answered, edit, del);
            actions.setSpacing(false);
            return actions;
        }).setHeader("Aktionen").setWidth("260px").setFlexGrow(0);

        grid.setMinWidth("1160px");
        grid.setSizeFull();

        // scrollbarer wrapper
        Div wrapper = new Div(grid);
        wrapper.getStyle().set("overflow-x", "auto");
        wrapper.setSizeFull();
        add(wrapper);
    }

    private void openForm(PrayerEntry existing) {
        Dialog dialog = new Dialog();
        dialog.setWidth("550px");
        dialog.setHeaderTitle(existing == null ? "Neues Gebet" : "Gebet bearbeiten");

        TextField titleField    = new TextField("Titel");
        TextArea  textField     = new TextArea("Gebetstext");
        TextField authorField   = new TextField("Autor");
        TextField categoryField = new TextField("Kategorie");
        Checkbox  visibleCheck  = new Checkbox("Sichtbar");

        textField.setMinHeight("120px");

        if (existing != null) {
            titleField.setValue(existing.getTitle());
            textField.setValue(existing.getPrayerText());
            authorField.setValue(existing.getAuthor());
            categoryField.setValue(existing.getCategory());
            visibleCheck.setValue(existing.isVisible());
        } else {
            visibleCheck.setValue(true);
            categoryField.setValue("Gebet");
        }

        FormLayout form = new FormLayout(titleField, authorField, categoryField, visibleCheck, textField);
        form.setColspan(textField, 2);
        form.setColspan(visibleCheck, 2);

        Button save = new Button("Speichern", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                if (existing == null) {
                    manager.addPrayer(authorField.getValue(), today, visibleCheck.getValue(),
                            categoryField.getValue(), titleField.getValue(), textField.getValue());
                    showSuccess("Gebet gespeichert!");
                } else {
                    existing.setTitle(titleField.getValue());
                    existing.setPrayerText(textField.getValue());
                    existing.setAuthor(authorField.getValue());
                    existing.setCategory(categoryField.getValue());
                    existing.setVisible(visibleCheck.getValue());
                    showSuccess("Gebet aktualisiert!");
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
        List<PrayerEntry> all = manager.getAllPrayers().stream()
                .filter(p -> {
                    String kw = searchField.getValue().toLowerCase().trim();
                    if (!kw.isBlank() && !p.getTitle().toLowerCase().contains(kw)
                            && !p.getAuthor().toLowerCase().contains(kw)) return false;
                    PrayerEntry.PrayerStatus s = statusFilter.getValue();
                    if (s != null && p.getStatus() != s) return false;
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
