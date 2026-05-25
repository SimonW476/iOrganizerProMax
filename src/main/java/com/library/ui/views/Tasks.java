package com.library.ui.views;

import com.library.backend.entities.Task;
import com.library.backend.entities.TaskRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("tasks")
@PageTitle("Tasks")
@Menu(order = 2, icon = "vaadin:check-square", title = "Tasks")
@PermitAll
public class Tasks extends VerticalLayout {
    private final TaskRepository taskRepo;
    private final Grid<Task> grid = new Grid<>(Task.class);

    public Tasks(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;

        TextField titleField = new TextField("Task");
        TextField dueDateField = new TextField("Due date");
        TextField priorityField = new TextField("Priority");

        Button addButton = new Button("Add Task");

        addButton.addClickListener(event -> {
            Task task = new Task(
                    titleField.getValue(),
                    dueDateField.getValue(),
                    priorityField.getValue(),
                    false
            );

            taskRepo.save(task);

            titleField.clear();
            dueDateField.clear();
            priorityField.clear();

            refreshGrid();
            Notification.show("Task added!");
        });

        grid.setColumns("title", "dueDate", "priority");

        grid.addComponentColumn(task -> {
            Checkbox checkbox = new Checkbox(task.isCompleted());
            checkbox.addValueChangeListener(event -> {
                task.setCompleted(event.getValue());
                taskRepo.save(task);
                refreshGrid();
            });
            return checkbox;
        }).setHeader("Done");

        add(titleField, dueDateField, priorityField, addButton, grid);

        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(taskRepo.findAll());
    }
}