package com.library.ui.views;

import com.library.backend.entities.Task;
import com.library.backend.service.TaskService;
import com.library.ui.layouts.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Route(value = "calendar", layout = MainLayout.class) // Loads inside the sidebar shell
@PageTitle("Calendar | ClearList")
@PermitAll
public class CalendarView extends VerticalLayout {

    public CalendarView(TaskService taskService) {
        setSizeFull();
        getStyle().set("overflow", "auto"); // Allows scrolling if you have many dates

        add(new H2("Upcoming Agenda"));

        List<Task> allTasks = taskService.getTasksForCurrentUser();

        // Filter out tasks without dates, and group them securely by their date string
        Map<String, List<Task>> groupedTasks = allTasks.stream()
                .filter(t -> t.getDueDate() != null && !t.getDueDate().trim().isEmpty())
                .collect(Collectors.groupingBy(Task::getDueDate));

        // Use a TreeMap to automatically sort the dates chronologically (earliest to latest)
        TreeMap<String, List<Task>> sortedGroups = new TreeMap<>(groupedTasks);

        if (sortedGroups.isEmpty()) {
            add(new Span("You have no tasks with upcoming due dates!"));
            return;
        }

        HorizontalLayout timeline = new HorizontalLayout();
        timeline.setSpacing(true);
        timeline.getStyle().set("padding-top", "10px").set("flex-wrap", "wrap");

        // Generate a visual "Column" for each date
        for (Map.Entry<String, List<Task>> entry : sortedGroups.entrySet()) {
            VerticalLayout dayColumn = new VerticalLayout();
            dayColumn.setWidth("300px");
            dayColumn.getStyle()
                    .set("background-color", "#f4f5f7")
                    .set("border-radius", "8px")
                    .set("padding", "16px");

            H4 dateHeader = new H4("📅 " + entry.getKey());
            dateHeader.getStyle().set("margin-top", "0");
            dayColumn.add(dateHeader);

            // Populate the tasks inside that date
            for (Task task : entry.getValue()) {
                VerticalLayout miniCard = new VerticalLayout();

                // Completed tasks will be grayed out slightly to show they are done
                miniCard.getStyle()
                        .set("background-color", task.isCompleted() ? "#f9fafb" : "white")
                        .set("padding", "10px")
                        .set("border-radius", "6px")
                        .set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)")
                        .set("margin-bottom", "8px");

                Span title = new Span(task.getTitle());
                title.getStyle().set("font-weight", "bold");

                if(task.isCompleted()) {
                    title.getStyle().set("text-decoration", "line-through").set("color", "gray");
                }

                Span priority = new Span("Priority: " + task.getPriority());
                priority.getStyle().set("font-size", "0.8em").set("color", "gray");

                miniCard.add(title, priority);
                dayColumn.add(miniCard);
            }
            timeline.add(dayColumn);
        }

        add(timeline);
    }
}