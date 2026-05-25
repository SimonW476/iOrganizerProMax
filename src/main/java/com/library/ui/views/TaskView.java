package com.library.ui.views;

import com.library.backend.entities.Task;
import com.library.backend.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("")
@PageTitle("My Tasks | ClearList")
@PermitAll
public class TaskView extends VerticalLayout {

    private final TaskService taskService;
    private final Grid<Task> grid = new Grid<>(Task.class, false);

    private final TextField titleField = new TextField("Task Title");
    private final DatePicker datePicker = new DatePicker("Due Date");
    private final ComboBox<String> priorityBox = new ComboBox<>("Priority");
    private final Button addButton = new Button("Add Task");

    public TaskView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        configureForm();
        configureGrid();

        HorizontalLayout formLayout = new HorizontalLayout(titleField, datePicker, priorityBox, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        add(formLayout, grid);

        updateList();
    }

    private void configureForm() {
        // Added a blank option ("") and updated to Title Case
        priorityBox.setItems("", "High", "Normal", "Low");
        priorityBox.setValue("Normal");

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(click -> saveTask());
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addComponentColumn(task -> {
            com.vaadin.flow.component.checkbox.Checkbox checkbox = new com.vaadin.flow.component.checkbox.Checkbox();
            checkbox.setValue(task.isCompleted());
            checkbox.addValueChangeListener(e -> {
                taskService.toggleTaskCompletion(task);
                updateList();
            });
            return checkbox;
        }).setHeader("Done").setFlexGrow(0).setWidth("80px");

        grid.addColumn(Task::getTitle).setHeader("Title");
        grid.addColumn(Task::getDueDate).setHeader("Due Date");
        grid.addColumn(Task::getPriority).setHeader("Priority");

        grid.addComponentColumn(task -> {
            Button deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteBtn.addClickListener(e -> {
                taskService.deleteTask(task);
                updateList();
            });
            return deleteBtn;
        }).setFlexGrow(0).setWidth("100px");
    }

    private void saveTask() {
        // Check if the title is empty and show a popup reminder if it is!
        if (titleField.isEmpty()) {
            Notification errorMsg = Notification.show("Please enter a task title.");
            errorMsg.addThemeVariants(NotificationVariant.LUMO_ERROR);
            errorMsg.setPosition(Notification.Position.TOP_CENTER);
            return; // Stops the method here so it doesn't save
        }

        String dateString = datePicker.getValue() != null ? datePicker.getValue().toString() : "";

        Task newTask = new Task(
                titleField.getValue(),
                dateString,
                priorityBox.getValue(),
                false
        );

        taskService.saveTask(newTask);

        titleField.clear();
        datePicker.clear();
        priorityBox.setValue("Normal"); // Reset to the Title Case default

        updateList();
    }

    private void updateList() {
        grid.setItems(taskService.getTasksForCurrentUser());
    }
}