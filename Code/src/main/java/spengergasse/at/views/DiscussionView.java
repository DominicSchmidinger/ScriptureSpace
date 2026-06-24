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
import spengergasse.at.model.DiscussionEntry;
import spengergasse.at.service.ScriptureManager;
import spengergasse.at.util.InvalidInputException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Diskussionen – Scripture Space")
@Route(value = "discussions", layout = MainLayout.class)
public class DiscussionView extends VerticalLayout {

    private final ScriptureManager manager;
    private final Grid<DiscussionEntry> grid = new Grid<>(DiscussionEntry.class, false);

    private final TextField searchField = new TextField();
    private final ComboBox<DiscussionEntry.DiscussionType> typeFilter = new ComboBox<>("Typ");

    public DiscussionView(ScriptureManager manager) {
        this.manager = manager;
        setSizeFull();
        setPadding(true);

        add(new H2("💬 Diskussionsfragen"));
        buildToolbar();
        buildGrid();
        refreshGrid();
    }

    private void buildToolbar() {
        searchField.setPlaceholder("Suche...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshGrid());

        typeFilter.setItems(DiscussionEntry.DiscussionType.values());
        typeFilter.setItemLabelGenerator(t -> switch (t) {
            case ESSENTIAL  -> "Wesentlich";
            case CASUAL     -> "Locker";
            case DEEP_FAITH -> "Tiefe Glaubensfrage";
        });
        typeFilter.setClearButtonVisible(true);
        typeFilter.addValueChangeListener(e -> refreshGrid());

        Button addBtn = new Button("Neue Frage", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openForm(null));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, typeFilter, addBtn);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();
        add(toolbar);
    }

    private void buildGrid() {
        grid.addColumn(DiscussionEntry::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(d -> d.getTypeLabel()).setHeader("Typ").setWidth("160px").setFlexGrow(0);
        grid.addColumn(DiscussionEntry::getQuestion).setHeader("Frage").setWidth("220px").setFlexGrow(0);
        grid.addColumn(DiscussionEntry::getContext).setHeader("Kontext").setWidth("130px").setFlexGrow(0);
        grid.addColumn(DiscussionEntry::getAuthor).setHeader("Autor").setWidth("110px").setFlexGrow(0);
        grid.addColumn(DiscussionEntry::getResponseCount).setHeader("Antworten").setWidth("100px").setFlexGrow(0);
        grid.addColumn(DiscussionEntry::getDateString).setHeader("Datum").setWidth("110px").setFlexGrow(0);

        grid.addComponentColumn(disc -> {
            Button respond = new Button("💬 Antwort");
            respond.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            respond.addClickListener(e -> {
                disc.addResponse();
                showSuccess("Antwort gezählt!");
                refreshGrid();
            });

            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            edit.addClickListener(e -> openForm(disc));

            Button del = new Button(VaadinIcon.TRASH.create());
            del.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            del.addClickListener(e -> {
                manager.removeById(disc.getId());
                showSuccess("Frage gelöscht");
                refreshGrid();
            });

            HorizontalLayout actions = new HorizontalLayout(respond, edit, del);
            actions.setSpacing(false);
            return actions;
        }).setHeader("Aktionen").setWidth("200px").setFlexGrow(0);

        grid.setMinWidth("1090px");
        grid.setSizeFull();

        Div wrapper = new Div(grid);
        wrapper.getStyle().set("overflow-x", "auto");
        wrapper.setSizeFull();
        add(wrapper);
    }

    private void openForm(DiscussionEntry existing) {
        Dialog dialog = new Dialog();
        dialog.setWidth("580px");
        dialog.setHeaderTitle(existing == null ? "Neue Diskussionsfrage" : "Frage bearbeiten");

        TextArea  questionField = new TextArea("Frage");
        TextField contextField  = new TextField("Bibelkontext (optional)");
        TextField authorField   = new TextField("Autor");
        TextField categoryField = new TextField("Kategorie");
        ComboBox<DiscussionEntry.DiscussionType> typeCombo = new ComboBox<>("Typ");
        typeCombo.setItems(DiscussionEntry.DiscussionType.values());
        typeCombo.setItemLabelGenerator(t -> switch (t) {
            case ESSENTIAL  -> "Wesentlich";
            case CASUAL     -> "Locker";
            case DEEP_FAITH -> "Tiefe Glaubensfrage";
        });
        Checkbox visibleCheck = new Checkbox("Sichtbar");

        questionField.setMinHeight("100px");

        if (existing != null) {
            questionField.setValue(existing.getQuestion());
            contextField.setValue(existing.getContext());
            authorField.setValue(existing.getAuthor());
            categoryField.setValue(existing.getCategory());
            typeCombo.setValue(existing.getType());
            visibleCheck.setValue(existing.isVisible());
        } else {
            visibleCheck.setValue(true);
            typeCombo.setValue(DiscussionEntry.DiscussionType.CASUAL);
            categoryField.setValue("Diskussion");
        }

        FormLayout form = new FormLayout(authorField, categoryField, typeCombo, contextField, visibleCheck, questionField);
        form.setColspan(questionField, 2);
        form.setColspan(visibleCheck, 2);

        Button save = new Button("Speichern", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                if (typeCombo.getValue() == null)
                    throw new InvalidInputException("Bitte einen Typ auswählen");
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                if (existing == null) {
                    manager.addDiscussion(authorField.getValue(), today, visibleCheck.getValue(),
                            categoryField.getValue(), questionField.getValue(),
                            contextField.getValue(), typeCombo.getValue());
                    showSuccess("Frage gespeichert!");
                } else {
                    existing.setQuestion(questionField.getValue());
                    existing.setContext(contextField.getValue());
                    existing.setAuthor(authorField.getValue());
                    existing.setCategory(categoryField.getValue());
                    existing.setType(typeCombo.getValue());
                    existing.setVisible(visibleCheck.getValue());
                    showSuccess("Frage aktualisiert!");
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
        List<DiscussionEntry> all = manager.getAllDiscussions().stream()
                .filter(d -> {
                    String kw = searchField.getValue().toLowerCase().trim();
                    if (!kw.isBlank() && !d.getQuestion().toLowerCase().contains(kw)
                            && !d.getAuthor().toLowerCase().contains(kw)) return false;
                    DiscussionEntry.DiscussionType t = typeFilter.getValue();
                    if (t != null && d.getType() != t) return false;
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
