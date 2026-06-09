package com.library.backend.service;

import com.library.backend.entities.ApplicationUser;
import com.library.backend.entities.BoardColumn;
import com.library.backend.entities.BoardColumnRepository;
import com.library.backend.entities.Task;
import com.library.backend.entities.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final BoardColumnRepository boardColumnRepo;

    public TaskService(TaskRepository taskRepository, UserService userService, BoardColumnRepository boardColumnRepo) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.boardColumnRepo = boardColumnRepo;
    }

    // --- TASK METHODS ---
    public List<Task> getTasksForCurrentUser() {
        Optional<ApplicationUser> currentUser = userService.getAuthenticatedUser();
        return currentUser.map(taskRepository::findByOwnerOrderByPositionIndexAsc).orElse(Collections.emptyList());
    }

    public Task saveTask(Task task) {
        Optional<ApplicationUser> currentUser = userService.getAuthenticatedUser();
        if (currentUser.isPresent()) {
            task.setOwner(currentUser.get());
            return taskRepository.save(task);
        }
        throw new IllegalStateException("No user found.");
    }

    public void saveAllTasks(List<Task> tasks) {
        taskRepository.saveAll(tasks);
    }

    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    // --- COLUMN METHODS ---
    public List<BoardColumn> getColumnsForCurrentUser() {
        Optional<ApplicationUser> user = userService.getAuthenticatedUser();
        if (user.isPresent()) {
            List<BoardColumn> cols = boardColumnRepo.findByOwnerOrderByPositionIndexAsc(user.get());
            // Auto-generate default columns if the user's board is empty
            if (cols.isEmpty()) {
                BoardColumn todo = new BoardColumn("To Do", 0, user.get());
                BoardColumn done = new BoardColumn("Done", 1, user.get());
                boardColumnRepo.saveAll(List.of(todo, done));
                return List.of(todo, done);
            }
            return cols;
        }
        return Collections.emptyList();
    }

    public BoardColumn saveColumn(BoardColumn col) {
        col.setOwner(userService.getAuthenticatedUser().orElseThrow());
        return boardColumnRepo.save(col);
    }

    public void saveAllColumns(List<BoardColumn> cols) {
        boardColumnRepo.saveAll(cols);
    }

    public void deleteColumn(BoardColumn col) {
        // Deletes all tasks inside the column before deleting the column itself
        List<Task> tasksInCol = getTasksForCurrentUser().stream()
                .filter(t -> t.getBoardColumn() != null && t.getBoardColumn().getId().equals(col.getId()))
                .toList();
        taskRepository.deleteAll(tasksInCol);
        boardColumnRepo.delete(col);
    }
}