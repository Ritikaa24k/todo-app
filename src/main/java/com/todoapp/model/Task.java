package com.todoapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// This is the model used to store a single item of a task 
public class Task {
    private static int nextId = 1;

    private int id;
    private String title;
    private String description;
    private boolean completed;
    private String priority; // [HIGH, MEDIUM, LOW]
    private String createdAt;
    private String dueDate;
    private String category;

    // Default constructor
    public Task() {
        this.id = nextId++;
        this.createdAt = LocalDateTime.now().toString();
        this.completed = false;
        this.priority = "LOW";
    }

    // Constructor with title/description
    public Task(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    // Full constructor
    public Task(String title, String description, String priority, LocalDateTime dueDate, String category) {
        this(title, description);
        this.priority = priority;
        this.dueDate = dueDate != null ? dueDate.toString() : null;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Business logic
    public boolean isOverdue() { 
        if (dueDate == null || completed) return false;
        try {
            LocalDateTime due = LocalDateTime.parse(dueDate);
            return LocalDateTime.now().isAfter(due);
        } catch (Exception e) {
            return false;
        }
    }

    public void markCompleted() {
        this.completed = true;
    }

    public void markIncomplete() {
        this.completed = false;
    }

    public String getPriorityDisplay() {
        switch (priority.toUpperCase()) {
            case "HIGH": return "🔴 HIGH";
            case "MEDIUM": return "🟡 MEDIUM";
            case "LOW": return "🟢 LOW";
            default: return priority;
        }
    }

    // Combined method for formatted date
    private String formatDate(String date) {
        if (date == null) return "No date";
        try {
            LocalDateTime dt = LocalDateTime.parse(date);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            return dt.format(formatter);
        } catch (Exception e) {
            return date;
        }
    }

    public String getFormattedDueDate() {
        return formatDate(dueDate);
    }

    public String getFormattedCreatedAt() {
        return formatDate(createdAt);
    }

    public String getStatusDisplay() {
        if (completed) return "✅ COMPLETED";
        else if (isOverdue()) return "⏰ OVERDUE";
        else return "📋 PENDING";
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s (%s) [%s]", id, title, getStatusDisplay(), getPriorityDisplay(), category != null ? category : "No Category");
    }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append("Task ID: ").append(id).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Description: ").append(description != null ? description : "No description").append("\n");
        sb.append("Status: ").append(getStatusDisplay()).append("\n");
        sb.append("Priority: ").append(getPriorityDisplay()).append("\n");
        sb.append("Category: ").append(category != null ? category : "No Category").append("\n");
        sb.append("Created: ").append(getFormattedCreatedAt()).append("\n");
        sb.append("Due Date: ").append(getFormattedDueDate()).append("\n");
        sb.append("=".repeat(50));
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
