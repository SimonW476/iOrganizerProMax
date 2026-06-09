package com.library.ui.views;

import com.library.backend.entities.Task;
import com.library.backend.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route("")
@PageTitle("My Tasks | ClearList")
@PermitAll
public class TaskView extends VerticalLayout {

    private final TaskService taskService;

    // The task currently being dragged
    private Task draggedTask;

    // Board Layouts
    private final VerticalLayout todoColumn = new VerticalLayout();
    private final VerticalLayout doneColumn = new VerticalLayout();

    // Form Components
    private final TextField titleField = new TextField("Task Title");
    private final DatePicker datePicker = new DatePicker("Due Date");
    private final ComboBox<String> priorityBox = new ComboBox<>("Priority");
    private final Button addButton = new Button("Add Task");

    public TaskView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        configureForm();

        // 1. Setup the Form Layout
        HorizontalLayout formLayout = new HorizontalLayout(titleField, datePicker, priorityBox, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        // 2. Setup the Board
        HorizontalLayout boardLayout = new HorizontalLayout();
        boardLayout.setSizeFull();
        boardLayout.setSpacing(true);

        configureColumn(todoColumn, "To Do", false);
        configureColumn(doneColumn, "Done", true);

        boardLayout.add(todoColumn, doneColumn);

        add(formLayout, boardLayout);

        refreshBoard();
    }

    private void configureForm() {
        priorityBox.setItems("", "High", "Normal", "Low");
        priorityBox.setValue("Normal");

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(click -> saveTask());
    }

    private void configureColumn(VerticalLayout column, String title, boolean isCompletedColumn) {
        column.setWidth("50%");
        column.setHeightFull(); // Forces the layout to expand

        column.getStyle()
                .set("min-height", "60vh") // Guarantees a massive drop zone even if empty
                .set("background-color", "#f4f5f7")
                .set("border-radius", "8px")
                .set("padding", "16px");

        H3 columnHeader = new H3(title);
        columnHeader.getStyle().set("margin-top", "0");
        column.add(columnHeader);

        // Setup the Drop Target
        DropTarget<VerticalLayout> dropTarget = DropTarget.create(column);
        dropTarget.setDropEffect(com.vaadin.flow.component.dnd.DropEffect.MOVE); // Tells the browser this accepts moving items

        dropTarget.addDropListener(event -> {
            event.getDragData().ifPresent(data -> {
                if (data instanceof Task) {
                    Task droppedTask = (Task) data;
                    if (droppedTask.isCompleted() != isCompletedColumn) {
                        droppedTask.setCompleted(isCompletedColumn);
                        taskService.toggleTaskCompletion(droppedTask);
                        refreshBoard();
                    }
                }
            });
        });
    }

    private Div createTaskCard(Task task) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("box-sizing", "border-box")
                .set("background-color", "white")
                .set("padding", "12px")
                .set("border-radius", "6px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.12)")
                .set("cursor", "grab")
                .set("margin-bottom", "8px");

        Div title = new Div();
        title.setText(task.getTitle());
        title.getStyle().set("font-weight", "bold");

        HorizontalLayout details = new HorizontalLayout();
        details.setSpacing(true);
        details.getStyle().set("margin-top", "8px").set("font-size", "0.85em");

        if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
            details.add(new Span("📅 " + task.getDueDate()));
        }
        if (task.getPriority() != null && !task.getPriority().isEmpty()) {
            Span prioritySpan = new Span("🔥 " + task.getPriority());
            details.add(prioritySpan);
        }

        Button deleteBtn = new Button("Delete", e -> {
            taskService.deleteTask(task);
            refreshBoard();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout footer = new HorizontalLayout(details, deleteBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        card.add(title, footer);

        // Make the card draggable and set explicit effects
        DragSource<Div> dragSource = DragSource.create(card);
        dragSource.setDragData(task);
        dragSource.setEffectAllowed(com.vaadin.flow.component.dnd.EffectAllowed.MOVE); // Explicitly allow moving

        return card;
    }

    private void saveTask() {
        if (titleField.isEmpty()) {
            Notification errorMsg = Notification.show("Please enter a task title.");
            errorMsg.addThemeVariants(NotificationVariant.LUMO_ERROR);
            errorMsg.setPosition(Notification.Position.TOP_CENTER);
            return;
        }

        String dateString = datePicker.getValue() != null ? datePicker.getValue().toString() : "";

        Task newTask = new Task(
                titleField.getValue(),
                dateString,
                priorityBox.getValue(),
                false // New tasks always start as incomplete
        );

        taskService.saveTask(newTask);

        titleField.clear();
        datePicker.clear();
        priorityBox.setValue("Normal");

        refreshBoard();
    }

    private void refreshBoard() {
        // Clear existing cards (leaving the H3 headers intact)
        todoColumn.getChildren()
                .filter(component -> component instanceof Div)
                .forEach(todoColumn::remove);

        doneColumn.getChildren()
                .filter(component -> component instanceof Div)
                .forEach(doneColumn::remove);

        List<Task> tasks = taskService.getTasksForCurrentUser();

        for (Task task : tasks) {
            Div card = createTaskCard(task);
            if (task.isCompleted()) {
                doneColumn.add(card);
            } else {
                todoColumn.add(card);
            }
        }
    }
}