package com.todoapp.service;
 
import com.todoapp.model.Task;
import com.todoapp.repository.FileRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Manages tasks and handles CRUD operations and task organization

public class TaskService {
    private List<Task> tasks;
    private FileRepository repository;

    public TaskService(){
        this.repository = new FileRepository("data/tasks.json");
        this.tasks = new ArrayList<>();
        loadTasks();
    }

    //constructor for testing
    public TaskService(FileRepository repository){
        this.repository = repository;
        this.tasks = new ArrayList<>();
        loadTasks();
    }

    // Load tasks from file storage
    public void loadTasks(){
        try{
            tasks = repository.loadTasks();
            //System.out.println("Successfully loaded " + tasks.size() + " tasks");
        }
        catch(Exception e){
            System.err.println("Error loading tasks: " + e.getMessage());
            tasks = new ArrayList<>();
        }
    }

    //save tasks to file storage - autosaves after operations to prevent data loss
    private void saveTasks(){
        try{
            repository.saveTasks(tasks);
            // FileRepository.saveTasks() returns void, not boolean
        }
        catch(Exception e){
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }


    //===========CORE TASK MANAGEMENT====================
    // create a new task with just title and description
    public Task createTask(String title, String description){
        if(title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        Task task = new Task(title.trim(), description != null ? description.trim() : "");
        tasks.add(task);
        saveTasks();
        return task;
    }

    //create a new task with all details
    public Task createTask(String title, String description, String priority, LocalDateTime dueDate, String category){
        if(title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        Task task = new Task(title.trim(), description != null ? description.trim() : "", priority != null ? priority.toUpperCase() : "LOW", dueDate, category != null ? category.trim() : null);
        
        tasks.add(task);
        saveTasks();
        return task;
    }

    //get all tasks
    public List<Task> getAllTasks(){
        return new ArrayList<>(tasks);
    }

    //Get a specific task by ID
    public Task getTaskById(int id) {
        return tasks.stream()
                   .filter(task -> task.getId() == id)
                   .findFirst()
                   .orElse(null);
    }

    //update an existing task(only with the fields provided)
    public boolean updateTask(int id, String title, String description, String priority, LocalDateTime dueDate, String category){
        Task task = getTaskById(id);
        if(task == null){
            return false;
        }
        if (title != null && !title.trim().isEmpty()) {
            task.setTitle(title.trim());
        }
        if (description != null) {
            task.setDescription(description.trim());
        }
        if (priority != null) {
            task.setPriority(priority.toUpperCase());
        }
        if (dueDate != null) {
            task.setDueDate(dueDate.toString());
        }
        if (category != null) {
            task.setCategory(category.trim());
        }

        saveTasks();
        return true;
    }

    //Delete a single task
    public boolean deleteTask(int id){
        boolean wasRemoved = tasks.removeIf(task -> task.getId() == id);
        if(wasRemoved){
            saveTasks();
        }
        return wasRemoved;
    }

    //==========TASK STATUS MANAGEMENT=============
    //Mark a task as completed
    public boolean completeTask(int id){
        Task task = getTaskById(id);
        if(task != null && !task.isCompleted()){
            task.markCompleted();
            saveTasks();
            return true;
        }
        return false;
    }

    //Mark a task as incomplete(can revert to pending)
    public boolean uncompleteTask(int id){
        Task task = getTaskById(id);
        if(task != null && task.isCompleted()){
            task.markIncomplete();
            saveTasks();
            return true;
        }
        return false;
    }

    // ======ORGANIZATION AND FILTERING========
    //Get all tasks in a specific category
    public List<Task> getTasksByCategory(String category){
        return tasks.stream().filter(task -> {
            String taskCategory = task.getCategory();
            if(category == null && taskCategory == null){
                return true;
            }
            return category != null && category.equalsIgnoreCase(taskCategory);
        }).collect(Collectors.toList());
    }

    // search tasks by title
    public List<Task> searchTasksByTitle(String searchTerm){
        if(searchTerm == null || searchTerm.trim().isEmpty()){
            return getAllTasks();
        }

        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        return tasks.stream().filter(task -> task.getTitle().toLowerCase().contains(lowerSearchTerm)).collect(Collectors.toList());
    }

    //=========BULK OPERATIONS==========
    //Remove all completed tasks
    public int clearCompletedTasks(){
        int removedCount = (int) tasks.stream().filter(Task::isCompleted).count();
        tasks.removeIf(Task::isCompleted);
        if(removedCount > 0){
            saveTasks();
            System.out.println("You have removed " + removedCount + " completed tasks");
        }
        return removedCount;
    }

    //Remove all tasks from the system
    public int clearAllTasks() {
        int removedCount = tasks.size();
        tasks.clear();
        
        if (removedCount > 0) {
            saveTasks();
            System.out.println("Removed all " + removedCount + " tasks");
        }
        
        return removedCount;
    }

    //========UTILITY METHODS=========
    /**
     * Check if there are any tasks
     */
    public boolean hasTasks() {
        return !tasks.isEmpty();
    }

    /**
     * Get the number of tasks
     */
    public int getTaskCount() {
        return tasks.size();
    }

//unit test
    // public static void main(String[] args) {
    //     TaskService service = new TaskService();
    
    //     System.out.println("Creating a sample task...");
    //     Task task = service.createTask("Finish project", "Complete TaskService class");
    
    //     System.out.println("Task created:");
    //     System.out.println(task);
    
    //     System.out.println("\nAll current tasks:");
    //     for (Task t : service.getAllTasks()) {
    //         System.out.println(t);
    //     }
    // }
}