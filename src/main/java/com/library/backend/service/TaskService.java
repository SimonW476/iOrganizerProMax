package com.library.backend.service;

import com.library.backend.entities.ApplicationUser;
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

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public List<Task> getTasksForCurrentUser() {
        Optional<ApplicationUser> currentUser = userService.getAuthenticatedUser();
        return currentUser.map(taskRepository::findByOwner).orElse(Collections.emptyList());
    }

    public Task saveTask(Task task) {
        Optional<ApplicationUser> currentUser = userService.getAuthenticatedUser();
        if (currentUser.isPresent()) {
            task.setOwner(currentUser.get());
            return taskRepository.save(task);
        }
        throw new IllegalStateException("No authenticated user found.");
    }

    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    public void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);
    }
}