package spengergasse.at.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import spengergasse.at.service.ScriptureManager;

import java.util.List;

/**
 * View für den Aktivitäts-Verlaufslog.
 */
@PageTitle("Aktivitätslog – Scripture Space")
@Route(value = "log", layout = MainLayout.class)
public class LogView extends VerticalLayout {

    private final ScriptureManager manager;
    private final Grid<String> grid = new Grid<>();

    public LogView(ScriptureManager manager) {
        this.manager = manager;
        setSizeFull();
        setPadding(true);

        add(new H2("📋 Aktivitätslog"));
        add(new Paragraph("Alle Aktionen werden hier protokolliert."));

        Button clearBtn = new Button("Log leeren");
        clearBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        clearBtn.addClickListener(e -> {
            manager.clearLog();
            refreshGrid();
            Notification n = Notification.show("Log geleert", 2000,
                    Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button refreshBtn = new Button("Aktualisieren");
        refreshBtn.addClickListener(e -> refreshGrid());

        add(new HorizontalLayout(refreshBtn, clearBtn));

        grid.addColumn(s -> s).setHeader("Eintrag").setAutoWidth(true);
        grid.setSizeFull();
        add(grid);

        refreshGrid();
    }

    private void refreshGrid() {
        List<String> log = manager.getActivityLog();
        // Neueste zuerst
        grid.setItems(log.stream().sorted((a, b) -> b.compareTo(a)).toList());
    }
}
