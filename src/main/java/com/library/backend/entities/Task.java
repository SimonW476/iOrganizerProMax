package com.library.backend.entities;

import jakarta.persistence.*;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String dueDate;
    private String priority;
    private boolean completed;

    // This links the task directly to a specific user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private ApplicationUser owner;

    public Task() {}

    public Task(String title, String dueDate, String priority, boolean completed) {
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    // New getters and setters for the owner
    public ApplicationUser getOwner() { return owner; }
    public void setOwner(ApplicationUser owner) { this.owner = owner; }
}