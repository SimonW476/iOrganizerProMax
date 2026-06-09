package com.library.backend.entities;

import jakarta.persistence.*;

@Entity
public class BoardColumn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer positionIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private ApplicationUser owner;

    public BoardColumn() {}

    public BoardColumn(String title, Integer positionIndex, ApplicationUser owner) {
        this.title = title;
        this.positionIndex = positionIndex;
        this.owner = owner;
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPositionIndex() { return positionIndex; }
    public void setPositionIndex(Integer positionIndex) { this.positionIndex = positionIndex; }

    public ApplicationUser getOwner() { return owner; }
    public void setOwner(ApplicationUser owner) { this.owner = owner; }
}