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
    private String category;

    // NEW: Links the task to a specific dynamic column
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "board_column_id")
    private BoardColumn boardColumn;

    private Integer positionIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private ApplicationUser owner;

    public Task() {}

    public Task(String title, String dueDate, String priority, String category, BoardColumn boardColumn) {
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.boardColumn = boardColumn;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getPositionIndex() { return positionIndex; }
    public void setPositionIndex(Integer positionIndex) { this.positionIndex = positionIndex; }
    public ApplicationUser getOwner() { return owner; }
    public void setOwner(ApplicationUser owner) { this.owner = owner; }

    public BoardColumn getBoardColumn() { return boardColumn; }
    public void setBoardColumn(BoardColumn boardColumn) { this.boardColumn = boardColumn; }
}