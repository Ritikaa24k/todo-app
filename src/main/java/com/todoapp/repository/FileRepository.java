package com.todoapp.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.todoapp.model.Task;
 
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


 // A simple file-based repository for managing task persistence.
 // Uses Gson to serialize/deserialize task lists to JSON.
public class FileRepository {
    private final String fileName;
    private final Gson gson;

    // Creates a FileRepository that manages tasks using the given file path.
    public FileRepository(String fileName){
        this.fileName = fileName;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        ensureFileExists();
    }

    //Ensures that the file and parent directory exist.
    private void ensureFileExists(){
        try {
            File file = new File(fileName);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
                saveTasks(new ArrayList<>()); // Start with empty list
            }
        } catch (IOException e) {
            System.err.println("Error initializing storage file: " + e.getMessage());
        }
    }

    // Saves a list of tasks to the JSON file.
    public void saveTasks(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.err.println("Failed to save tasks: " + e.getMessage());
        }
    }

    //Loads tasks from the JSON file.
    public List<Task> loadTasks() {
        try {
            File file = new File(fileName);
            if (!file.exists() || file.length() == 0) return new ArrayList<>();

            String content = Files.readString(Paths.get(fileName));
            if (content.trim().isEmpty()) return new ArrayList<>();

            Type listType = new TypeToken<List<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(content, listType);
            return tasks != null ? tasks : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load tasks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //unit test  to demonstrate saving and loading.

    // public static void main(String[] args) {
    //     FileRepository repo = new FileRepository("data/tasks.json");

    //     Task task1 = new Task("Complete Resume", "Update skills and format", "HIGH", null, "Career");
    //     Task task2 = new Task("Buy groceries", "Milk, eggs, bread", "MEDIUM", null, "Personal");

    //     List<Task> tasks = new ArrayList<>();
    //     tasks.add(task1);
    //     tasks.add(task2);

    //     repo.saveTasks(tasks);
    //     System.out.println("✅ Tasks saved.");

    //     List<Task> loaded = repo.loadTasks();
    //     System.out.println("📋 Tasks loaded:");
    //     for (Task t : loaded) {
    //         System.out.println(t.toDetailedString());
    //     }
    // }
}
