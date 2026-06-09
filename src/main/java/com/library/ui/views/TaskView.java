package com.library.ui.views;

import com.library.backend.entities.BoardColumn;
import com.library.backend.entities.Task;
import com.library.backend.service.TaskService;
import com.library.ui.layouts.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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

@Route(value = "", layout = MainLayout.class)
@PageTitle("My Tasks | ClearList")
@PermitAll
public class TaskView extends VerticalLayout {

    private final TaskService taskService;
    private TaskCard lastDroppedCard = null;

    private final HorizontalLayout boardLayout = new HorizontalLayout();

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

        boardLayout.setSizeFull();
        boardLayout.setSpacing(true);
        boardLayout.getStyle().set("overflow-x", "auto").set("padding-bottom", "20px");

        add(formLayout, filterLayout, boardLayout);

        refreshBoard();
    }

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

    private VerticalLayout createColumnLayout(BoardColumn col) {
        VerticalLayout columnWrapper = new VerticalLayout();
        columnWrapper.setWidth("350px");
        columnWrapper.setMinWidth("350px"); // Prevents squishing when you add lots of columns
        columnWrapper.setHeightFull();
        columnWrapper.getStyle().set("min-height", "60vh").set("background-color", "#f4f5f7").set("border-radius", "8px").set("padding", "16px");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        H3 title = new H3(col.getTitle());
        title.getStyle().set("margin", "0");

        Button leftBtn = new Button("←", e -> moveColumn(col, -1));
        leftBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        Button rightBtn = new Button("→", e -> moveColumn(col, 1));
        rightBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        Button delBtn = new Button("✖", e -> {
            taskService.deleteColumn(col);
            refreshBoard();
        });
        delBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);

        HorizontalLayout controls = new HorizontalLayout(leftBtn, rightBtn, delBtn);
        header.add(title, controls);
        columnWrapper.add(header);

        DropTarget<VerticalLayout> dropTarget = DropTarget.create(columnWrapper);
        dropTarget.setDropEffect(com.vaadin.flow.component.dnd.DropEffect.MOVE);
        dropTarget.addDropListener(event -> {
            event.getDragSourceComponent().ifPresent(draggedComponent -> {
                if (draggedComponent instanceof TaskCard draggedCard) {
                    if (lastDroppedCard == draggedCard) return;

                    Task task = draggedCard.getTask();
                    task.setBoardColumn(col);
                    columnWrapper.add(draggedCard);
                    recalculateAndSavePositions(col, columnWrapper);
                }
            });
        });

        return columnWrapper;
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

        DragSource<TaskCard> dragSource = DragSource.create(card);
        dragSource.setEffectAllowed(com.vaadin.flow.component.dnd.EffectAllowed.MOVE);
        dragSource.addDragEndListener(e -> lastDroppedCard = null);

        DropTarget<TaskCard> dropTarget = DropTarget.create(card);
        dropTarget.setDropEffect(com.vaadin.flow.component.dnd.DropEffect.MOVE);
        dropTarget.addDropListener(event -> {
            event.getDragSourceComponent().ifPresent(draggedComponent -> {
                if (draggedComponent instanceof TaskCard draggedCard && draggedCard != card) {
                    lastDroppedCard = draggedCard;

                    Task draggedTask = draggedCard.getTask();
                    draggedTask.setBoardColumn(task.getBoardColumn());

                    VerticalLayout targetColumn = (VerticalLayout) card.getParent().orElse(null);
                    if (targetColumn != null) {
                        int targetIndex = targetColumn.indexOf(card);
                        targetColumn.addComponentAtIndex(targetIndex, draggedCard);
                        recalculateAndSavePositions(task.getBoardColumn(), targetColumn);
                    }
                }
            });
        });

        return card;
    }

    private void recalculateAndSavePositions(BoardColumn col, VerticalLayout columnWrapper) {
        List<Task> updatedTasks = new ArrayList<>();
        int index = 0;
        for (Component c : columnWrapper.getChildren().toList()) {
            if (c instanceof TaskCard) {
                Task t = ((TaskCard) c).getTask();
                t.setPositionIndex(index++);
                t.setBoardColumn(col);
                updatedTasks.add(t);
            }
        }
        taskService.saveAllTasks(updatedTasks);
    }

    private void moveColumn(BoardColumn col, int direction) {
        List<BoardColumn> cols = taskService.getColumnsForCurrentUser();
        int index = -1;
        for(int i=0; i<cols.size(); i++){
            if(cols.get(i).getId().equals(col.getId())) index = i;
        }

        int newIndex = index + direction;
        if (newIndex >= 0 && newIndex < cols.size()) {
            BoardColumn tempCol = cols.remove(index);
            cols.add(newIndex, tempCol);

            // Reassign the position indices and save
            for(int i=0; i<cols.size(); i++){
                cols.get(i).setPositionIndex(i);
            }
            taskService.saveAllColumns(cols);
            refreshBoard();
        }
    }

    private void promptNewColumn() {
        Dialog dialog = new Dialog();
        TextField colName = new TextField("New Column Name");
        Button save = new Button("Save", e -> {
            if (!colName.isEmpty()) {
                int position = taskService.getColumnsForCurrentUser().size();
                BoardColumn newCol = new BoardColumn(colName.getValue(), position, null);
                taskService.saveColumn(newCol);
                dialog.close();
                refreshBoard();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout layout = new VerticalLayout(colName, save);
        dialog.add(layout);
        dialog.open();
    }

    private void saveTask() {
        if (titleField.isEmpty()) {
            Notification errorMsg = Notification.show("Please enter a task title.");
            errorMsg.addThemeVariants(NotificationVariant.LUMO_ERROR);
            errorMsg.setPosition(Notification.Position.TOP_CENTER);
            return;
        }

        List<BoardColumn> cols = taskService.getColumnsForCurrentUser();
        if(cols.isEmpty()) return; // Failsafe

        String dateString = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
        Task newTask = new Task(titleField.getValue(), dateString, priorityBox.getValue(), categoryBox.getValue(), cols.get(0));

        taskService.saveTask(newTask);

        titleField.clear();
        datePicker.clear();
        priorityBox.setValue("Normal");
        categoryBox.setValue("General");

        refreshBoard();
    }

    private void refreshBoard() {
        boardLayout.removeAll();

        List<BoardColumn> columns = taskService.getColumnsForCurrentUser();
        List<Task> allTasks = taskService.getTasksForCurrentUser();

        if (filterCategory.getValue() != null && !filterCategory.getValue().isEmpty()) {
            allTasks = allTasks.stream().filter(t -> filterCategory.getValue().equals(t.getCategory())).collect(Collectors.toList());
        }
        if (filterDate.getValue() != null) {
            String filterDateString = filterDate.getValue().toString();
            allTasks = allTasks.stream().filter(t -> filterDateString.equals(t.getDueDate())).collect(Collectors.toList());
        }

        // Build the dynamic columns
        for (BoardColumn col : columns) {
            VerticalLayout columnLayout = createColumnLayout(col);

            List<Task> tasksInCol = allTasks.stream()
                    .filter(t -> t.getBoardColumn() != null && t.getBoardColumn().getId().equals(col.getId()))
                    .collect(Collectors.toList());

            for (Task t : tasksInCol) {
                columnLayout.add(createTaskCard(t));
            }
            boardLayout.add(columnLayout);
        }

        // The button to create new columns!
        Button addColBtn = new Button("+ Add Column", e -> promptNewColumn());
        addColBtn.setHeight("60px");
        addColBtn.getStyle().set("margin-top", "16px");
        boardLayout.add(addColBtn);
    }
}