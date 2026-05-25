package com.library.backend.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // This custom method allows TaskService to fetch only the logged-in user's tasks
    List<Task> findByOwner(ApplicationUser owner);
}