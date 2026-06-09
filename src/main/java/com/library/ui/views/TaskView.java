package com.library.ui.views;

import com.library.backend.entities.Task;
import com.library.backend.service.TaskService;
import com.vaadin.flow.component.Component;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.library.ui.layouts.MainLayout;

@Route(value = "", layout = MainLayout.class)
@PageTitle("My Tasks | ClearList")
@PermitAll
public class TaskView extends VerticalLayout {

    private final TaskService taskService;
    private TaskCard lastDroppedCard = null; // Prevents "double dropping" glitches

    private final VerticalLayout todoColumn = new VerticalLayout();
    private final VerticalLayout doneColumn = new VerticalLayout();

    private final TextField titleField = new TextField("Task Title");
    private final DatePicker datePicker = new DatePicker("Due Date");
    private final ComboBox<String> priorityBox = new ComboBox<>("Priority");
    private final ComboBox<String> categoryBox = new ComboBox<>("Category");
    private final Button addButton = new Button("Add Task");

    private final ComboBox<String> filterCategory = new ComboBox<>("Filter by Category");
    private final DatePicker filterDate = new DatePicker("Filter by Date");
    private final Button clearFiltersBtn = new Button("Clear Filters");

    public TaskView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        configureForm();
        configureFilters();

        HorizontalLayout formLayout = new HorizontalLayout(titleField, datePicker, priorityBox, categoryBox, addButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        HorizontalLayout filterLayout = new HorizontalLayout(filterCategory, filterDate, clearFiltersBtn);
        filterLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        filterLayout.getStyle().set("margin-top", "20px").set("padding", "10px").set("background-color", "#f9fafb").set("border-radius", "8px");

        HorizontalLayout boardLayout = new HorizontalLayout();
        boardLayout.setSizeFull();
        boardLayout.setSpacing(true);

        configureColumn(todoColumn, "To Do", false);
        configureColumn(doneColumn, "Done", true);

        boardLayout.add(todoColumn, doneColumn);

        add(formLayout, filterLayout, boardLayout);

        refreshBoard();
    }

    // Custom UI Component that holds a direct reference to the database Task
    private static class TaskCard extends Div {
        private final Task task;
        public TaskCard(Task task) { this.task = task; }
        public Task getTask() { return task; }
    }

    private void configureForm() {
        priorityBox.setItems("", "High", "Normal", "Low");
        priorityBox.setValue("Normal");
        categoryBox.setItems("Homework", "Project", "Errands", "Chores", "General");
        categoryBox.setValue("General");

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(click -> saveTask());
    }

    private void configureFilters() {
        filterCategory.setItems("Homework", "Project", "Errands", "Chores", "General");
        filterCategory.setPlaceholder("All Categories");
        filterCategory.addValueChangeListener(e -> refreshBoard());

        filterDate.setPlaceholder("Any Date");
        filterDate.addValueChangeListener(e -> refreshBoard());

        clearFiltersBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFiltersBtn.addClickListener(e -> {
            filterCategory.clear();
            filterDate.clear();
            refreshBoard();
        });
    }

    private void configureColumn(VerticalLayout column, String title, boolean isCompletedColumn) {
        column.setWidth("50%");
        column.setHeightFull();
        column.getStyle().set("min-height", "60vh").set("background-color", "#f4f5f7").set("border-radius", "8px").set("padding", "16px");

        H3 columnHeader = new H3(title);
        columnHeader.getStyle().set("margin-top", "0");
        column.add(columnHeader);

        DropTarget<VerticalLayout> dropTarget = DropTarget.create(column);
        dropTarget.setDropEffect(com.vaadin.flow.component.dnd.DropEffect.MOVE);

        // Handles dropping a card into the empty space at the bottom of a column
        dropTarget.addDropListener(event -> {
            event.getDragSourceComponent().ifPresent(draggedComponent -> {
                if (draggedComponent instanceof TaskCard draggedCard) {
                    if (lastDroppedCard == draggedCard) return; // Prevent double firing if dropped on a card

                    Task task = draggedCard.getTask();
                    task.setCompleted(isCompletedColumn);
                    column.add(draggedCard); // Appends to the very bottom
                    recalculateAndSavePositions();
                }
            });
        });
    }

    private TaskCard createTaskCard(Task task) {
        TaskCard card = new TaskCard(task);
        card.setWidthFull();
        card.getStyle()
                .set("box-sizing", "border-box").set("background-color", "white").set("padding", "12px")
                .set("border-radius", "6px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.12)")
                .set("cursor", "grab").set("margin-bottom", "8px").set("user-select", "none");

        Div title = new Div();
        title.setText(task.getTitle());
        title.getStyle().set("font-weight", "bold");

        HorizontalLayout details = new HorizontalLayout();
        details.setSpacing(true);
        details.getStyle().set("margin-top", "8px").set("font-size", "0.85em");

        if (task.getDueDate() != null && !task.getDueDate().isEmpty()) details.add(new Span("📅 " + task.getDueDate()));
        if (task.getPriority() != null && !task.getPriority().isEmpty()) details.add(new Span("🔥 " + task.getPriority()));
        if (task.getCategory() != null && !task.getCategory().isEmpty()) {
            Span catSpan = new Span("🏷️ " + task.getCategory());
            catSpan.getStyle().set("color", "gray");
            details.add(catSpan);
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

        // Make the Card Draggable
        DragSource<TaskCard> dragSource = DragSource.create(card);
        dragSource.setEffectAllowed(com.vaadin.flow.component.dnd.EffectAllowed.MOVE);
        dragSource.addDragEndListener(e -> lastDroppedCard = null); // Reset security flag

        // Make the Card a Drop Target for exact reordering
        DropTarget<TaskCard> dropTarget = DropTarget.create(card);
        dropTarget.setDropEffect(com.vaadin.flow.component.dnd.DropEffect.MOVE);
        dropTarget.addDropListener(event -> {
            event.getDragSourceComponent().ifPresent(draggedComponent -> {
                if (draggedComponent instanceof TaskCard draggedCard && draggedCard != card) {
                    lastDroppedCard = draggedCard; // Flags that the card handled the drop

                    Task draggedTask = draggedCard.getTask();
                    draggedTask.setCompleted(task.isCompleted());

                    VerticalLayout targetColumn = task.isCompleted() ? doneColumn : todoColumn;
                    int targetIndex = targetColumn.indexOf(card);

                    // Insert the dragged card exactly where it was dropped
                    targetColumn.addComponentAtIndex(targetIndex, draggedCard);
                    recalculateAndSavePositions();
                }
            });
        });

        return card;
    }

    // Silently loops through the visible columns, assigns an index value, and saves it to the DB
    private void recalculateAndSavePositions() {
        List<Task> updatedTasks = new ArrayList<>();

        int index = 0;
        for (Component c : todoColumn.getChildren().toList()) {
            if (c instanceof TaskCard) {
                Task t = ((TaskCard) c).getTask();
                t.setPositionIndex(index++);
                updatedTasks.add(t);
            }
        }

        index = 0;
        for (Component c : doneColumn.getChildren().toList()) {
            if (c instanceof TaskCard) {
                Task t = ((TaskCard) c).getTask();
                t.setPositionIndex(index++);
                updatedTasks.add(t);
            }
        }

        taskService.saveAllTasks(updatedTasks);
    }

    private void saveTask() {
        if (titleField.isEmpty()) {
            Notification errorMsg = Notification.show("Please enter a task title.");
            errorMsg.addThemeVariants(NotificationVariant.LUMO_ERROR);
            errorMsg.setPosition(Notification.Position.TOP_CENTER);
            return;
        }

        String dateString = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
        Task newTask = new Task(titleField.getValue(), dateString, priorityBox.getValue(), categoryBox.getValue(), false);

        taskService.saveTask(newTask);

        titleField.clear();
        datePicker.clear();
        priorityBox.setValue("Normal");
        categoryBox.setValue("General");

        refreshBoard();
    }

    private void refreshBoard() {
        todoColumn.getChildren().filter(component -> component instanceof TaskCard).forEach(todoColumn::remove);
        doneColumn.getChildren().filter(component -> component instanceof TaskCard).forEach(doneColumn::remove);

        List<Task> tasks = taskService.getTasksForCurrentUser();

        if (filterCategory.getValue() != null && !filterCategory.getValue().isEmpty()) {
            tasks = tasks.stream().filter(t -> filterCategory.getValue().equals(t.getCategory())).collect(Collectors.toList());
        }

        if (filterDate.getValue() != null) {
            String filterDateString = filterDate.getValue().toString();
            tasks = tasks.stream().filter(t -> filterDateString.equals(t.getDueDate())).collect(Collectors.toList());
        }

        for (Task task : tasks) {
            TaskCard card = createTaskCard(task);
            if (task.isCompleted()) doneColumn.add(card);
            else todoColumn.add(card);
        }
    }
}