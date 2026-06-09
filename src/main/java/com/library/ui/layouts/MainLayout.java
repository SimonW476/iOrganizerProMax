package com.library.ui.layouts;

import com.library.ui.views.CalendarView;
import com.library.ui.views.TaskView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll; // Added Import

@PermitAll // Added Annotation: Allows logged-in users to see the shell!
public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("ClearList");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        Button logoutBtn = new Button("Log out", e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logoutBtn);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("My Tasks", TaskView.class));
        nav.addItem(new SideNavItem("Calendar", CalendarView.class));

        addToDrawer(nav);
    }
}