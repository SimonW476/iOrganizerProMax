package com.library.ui.views;

import com.library.backend.entities.Task;
import com.library.backend.service.TaskService;
import com.library.ui.layouts.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.time.LocalDate;
import java.util.List;

@Route(value = "calendar", layout = MainLayout.class)
@PageTitle("Calendar | ClearList")
@PermitAll
public class CalendarView extends VerticalLayout {

    public CalendarView(TaskService taskService) {
        setSizeFull();
        setPadding(false);

        System.out.println("--- LOADING CALENDAR VIEW ---");

        // 1. Build the FullCalendar grid
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        // FIX 1: Explicitly force the calendar to expand so it doesn't collapse to 0 pixels
        calendar.setSizeFull();

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();

        // 2. Fetch Tasks
        List<Task> tasks = taskService.getTasksForCurrentUser();
        System.out.println("Found " + tasks.size() + " total tasks in the database.");

        for (Task task : tasks) {
            if (task.getDueDate() != null && !task.getDueDate().trim().isEmpty()) {
                try {
                    System.out.println("Attempting to parse date for task: " + task.getTitle() + " [" + task.getDueDate() + "]");

                    Entry entry = new Entry();
                    entry.setTitle(task.getTitle());

                    // Use atStartOfDay() just in case the plugin requires a specific time footprint
                    entry.setStart(LocalDate.parse(task.getDueDate()).atStartOfDay());
                    entry.setAllDay(true);

                    boolean isDone = task.getBoardColumn() != null &&
                            task.getBoardColumn().getTitle().equalsIgnoreCase("Done");

                    if (isDone) {
                        entry.setColor("#bdc3c7");
                    } else {
                        switch (task.getCategory() == null ? "" : task.getCategory()) {
                            case "Homework": entry.setColor("#e74c3c"); break;
                            case "Project": entry.setColor("#9b59b6"); break;
                            case "Errands": entry.setColor("#f1c40f"); break;
                            case "Chores": entry.setColor("#e67e22"); break;
                            default: entry.setColor("#3498db"); break;
                        }
                    }

                    entryProvider.addEntries(entry);
                    System.out.println("Successfully added to calendar: " + task.getTitle());

                } catch (Exception e) {
                    // FIX 2: Print the exact error so it doesn't fail silently!
                    System.err.println("FAILED to parse date for task: " + task.getTitle());
                    e.printStackTrace();
                }
            }
        }

        entryProvider.refreshAll();

        add(calendar);

        // FIX 1b: Force the parent layout to give all remaining vertical space to the calendar
        setFlexGrow(1, calendar);
    }
}