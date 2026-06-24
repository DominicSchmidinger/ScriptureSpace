package spengergasse.at.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Haupt-Layout mit Seitennavigation für alle Views.
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createNavigation();
    }

    private void createHeader() {
        H1 title = new H1("✝ Scripture Space");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createNavigation() {
        SideNav nav = new SideNav();

        SideNavItem dashboard = new SideNavItem("Dashboard",
                DashboardView.class, VaadinIcon.HOME.create());
        SideNavItem verses = new SideNavItem("Bibelverse",
                VerseView.class, VaadinIcon.BOOK.create());
        SideNavItem prayers = new SideNavItem("Gebete",
                PrayerView.class, VaadinIcon.HEART.create());
        SideNavItem discussions = new SideNavItem("Diskussionen",
                DiscussionView.class, VaadinIcon.COMMENTS.create());
        SideNavItem testimonies = new SideNavItem("Zeugnisse",
                TestimonyView.class, VaadinIcon.STAR.create());
        SideNavItem log = new SideNavItem("Aktivitätslog",
                LogView.class, VaadinIcon.LIST.create());

        nav.addItem(dashboard, verses, prayers, discussions, testimonies, log);
        addToDrawer(nav);
    }
}
