package com.library.backend.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Automatically sorts the tasks exactly how the user arranged them
    List<Task> findByOwnerOrderByPositionIndexAsc(ApplicationUser owner);
}