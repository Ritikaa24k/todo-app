package com.todoapp;

import com.todoapp.model.Task;
import com.todoapp.service.TaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command-based Todo List application.
 * Simple command-line interface for managing todo tasks using commands.
 * 
 * @author Ritikaa Kailas 
 */
public class TodoApp {
    private TaskService taskService = new TaskService();
    private Scanner scanner = new Scanner(System.in);
    
    // Table formatting constants
    private static final String TABLE_BORDER= "+------+---------------------------+---------------+----------+------------+------------------+--------+";
    private static final String TABLE_HEADER= "| ID   | Title                     | Category      | Priority | Status     | Due Date         | Comp.  |";
    
    public static void main(String[] args) {
        TodoApp app = new TodoApp();
        
        if (args.length == 0) {
            app.showWelcome();
            app.runInteractive();
        } else {
            app.executeCommand(args);
        }
    }
    
    private void showWelcome() {
        System.out.println("======================================");
        System.out.println("TODO LIST APPLICATION - COMMAND MODE");
        System.out.println("======================================");
        showStats();
        System.out.println("\nType 'help' to see available commands or 'exit' to quit.");
    }
    
    private void runInteractive() {
        while (true) {
            System.out.print("\ntodo> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] args = parseCommand(input);
            if (args[0].equals("exit")) {
                System.out.println("Thanks for using Todo List! 👋");
                break;
            }
            try {
                executeCommand(args);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
    
    private String[] parseCommand(String input) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Quoted string
                tokens.add(matcher.group(1));
            } else {
                // Unquoted word
                tokens.add(matcher.group(2));
            }
        }
        return tokens.toArray(new String[0]);
    }
    
    private void executeCommand(String[] args) {
        String command = args[0].toLowerCase();
        switch (command) {
            case "help":
            case "h":
                showHelp();
                break;
            case "add":
            case "a":
                handleAdd(args);
                break;
            case "list":
            case "ls":
            case "l":
                handleList(args);
                break;
            case "show":
            case "view":
            case "s":
                handleShow(args);
                break;
            case "complete":
            case "done":
            case "c":
                handleComplete(args);
                break;
            case "uncomplete":
            case "undone":
            case "u":
                handleUncomplete(args);
                break;
            case "update":
            case "edit":
            case "up":
                handleUpdate(args);
                break;
            case "delete":
            case "remove":
            case "rm":
            case "d":
                handleDelete(args);
                break;
            case "search":
            case "find":
            case "f":
                handleSearch(args);
                break;
            case "category":
            case "cat":
                handleCategory(args);
                break;
            case "clear":
                handleClear(args);
                break;
            case "stats":
            case "status":
                showStats();
                break;
            default:
                System.out.println("❌ Unknown command: " + command);
                System.out.println("Type 'help' to see available commands.");
        }
    }
    
    private void showHelp() {
        System.out.println("\n📋 AVAILABLE COMMANDS:");
        System.out.println("======================================");
        System.out.println("add <title> [description]           - Add new task");
        System.out.println("  Examples:");
        System.out.println("    add \"Buy groceries\"");
        System.out.println("    add \"Study Java\" \"Complete chapter 5\"");
        System.out.println("list                                 - Show all tasks");
        System.out.println("show <id>                           - Show task details");
        System.out.println("complete <id>                       - Mark task as complete");
        System.out.println("uncomplete <id>                     - Mark task as incomplete");
        System.out.println("update <id>                         - Update task (interactive)");
        System.out.println("delete <id>                         - Delete task");
        System.out.println("search <term>                       - Search tasks by title");
        System.out.println("category <name>                     - Show tasks by category");
        System.out.println("clear completed                     - Remove completed tasks");
        System.out.println("clear all                           - Remove all tasks");
        System.out.println("stats                               - Show task statistics");
        System.out.println("help                                - Show this help");
        System.out.println("exit                                - Exit application");
        System.out.println("\nAliases: a=add, l/ls=list, s=show, c=complete, u=uncomplete,");
        System.out.println("         up=update, d/rm=delete, f=find, cat=category");
        System.out.println("\nTip: Use quotes for multi-word titles: add \"Make CLI app\"");
    }
    
    private void handleAdd(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: add <title> [description]");
            System.out.println("   Example: add \"Make CLI app\" \"Java command line application\"");
            return;
        }
        
        String title = args[1];
        String description = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "";
        
        try {
            Task task = taskService.createTask(title, description);
            System.out.println("✅ Task created successfully!");
            displaySingleTaskTable(task);
            System.out.print("Add more details? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y")||response.equals("yes")) {
                addDetailsToTask(task);
            }
        } catch (Exception e) {
            System.out.println("❌ Error creating task: " + e.getMessage());
        }
    }
    
    private void addDetailsToTask(Task existingTask) {
        System.out.println("\n📝 Adding details to task: " + existingTask.getTitle());
        
        // Priority
        System.out.print("Priority (HIGH/MEDIUM/LOW) [" + existingTask.getPriority() + "]: ");
        String priority = scanner.nextLine().trim();
        if (!priority.isEmpty()) {
            existingTask.setPriority(priority.toUpperCase());
        }
        
        // Due date with validation loop
        LocalDateTime dueDate = null;
        while (true) {
            System.out.print("Due date (YYYY-MM-DD HH:MM) or press Enter to skip: ");
            String dueDateStr = scanner.nextLine().trim();
            if (dueDateStr.isEmpty()) {
                break; 
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                dueDate = LocalDateTime.parse(dueDateStr, formatter);
                if (dueDate.isBefore(LocalDateTime.now())) {
                    System.out.println("⚠️ Due date is in the past! Please enter a future date.");
                    System.out.print("Continue with past date anyway? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (confirm.equals("y")||confirm.equals("yes")) {
                        existingTask.setDueDate(dueDate.toString());
                        break;
                    }
                } else {
                    existingTask.setDueDate(dueDate.toString());
                    break;
                }
            }
            catch (DateTimeParseException e) {
                System.out.println("⚠️ Invalid date format! Please use YYYY-MM-DD HH:MM format.");
                System.out.println("   Example: 2025-06-25 14:30");
            }
        }
        // Category
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) {
            existingTask.setCategory(category);
        }
        // Update the task
        taskService.updateTask(existingTask.getId(), 
                              existingTask.getTitle(), 
                              existingTask.getDescription(),
                              existingTask.getPriority(),
                              dueDate,
                              existingTask.getCategory());
        System.out.println("✅ Task details updated!");
        displaySingleTaskTable(existingTask);
    }
    
    private void handleList(String[] args) {
        List<Task> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("📝 No tasks found. Use 'add \"<title>\"' to create your first task!");
            return;
        }
        System.out.println("\n📋 ALL TASKS:");
        displayTasksTable(tasks);
        System.out.println("Total: " + tasks.size() + " tasks");
    }
    
    private void handleShow(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: show <id>");
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            Task task = taskService.getTaskById(id);
            if (task == null) {
                System.out.println("❌ Task with ID " + id + " not found.");
                System.out.println("💡 Use 'list' to see all available task IDs.");
                return;
            }
            showTaskDetails(task);
        }
        catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID. Please enter a number.");
        }
    }
    
    private void handleComplete(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: complete <id>");
            return;
        }
        
        try {
            int id = Integer.parseInt(args[1]);
            if (taskService.completeTask(id)) {
                System.out.println("✅ Task " + id + " marked as complete!");
            }
            else {
                System.out.println("❌ Task " + id + " not found or already completed.");
            }
        }
        catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID. Please enter a number.");
        }
    }
    
    private void handleUncomplete(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: uncomplete <id>");
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            if (taskService.uncompleteTask(id)) {
                System.out.println("✅ Task " + id + " marked as incomplete!");
            }
            else {
                System.out.println("❌ Task " + id + " not found or already incomplete.");
            }
        }
        catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID. Please enter a number.");
        }
    }
    
    private void handleUpdate(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: update <id>");
            return;
        }
        
        try {
            int id = Integer.parseInt(args[1]);
            Task task = taskService.getTaskById(id);
            if (task == null) {
                System.out.println("❌ Task " + id + " not found.");
                return;
            }
            System.out.println("Current task:");
            displaySingleTaskTable(task);
            
            System.out.print("New title [" + task.getTitle() + "]: ");
            String title = scanner.nextLine().trim();
            
            System.out.print("New description [" + task.getDescription() + "]: ");
            String description = scanner.nextLine().trim();
            
            System.out.print("New priority [" + task.getPriority() + "]: ");
            String priority = scanner.nextLine().trim();
            
            System.out.print("New category [" + (task.getCategory() != null ? task.getCategory() : "None") + "]: ");
            String category = scanner.nextLine().trim();
            
            if (taskService.updateTask(id,
                    title.isEmpty() ? null : title,
                    description.isEmpty() ? null : description,
                    priority.isEmpty() ? null : priority,
                    null,
                    category.isEmpty() ? null : category)) {
                
                System.out.println("✅ Task updated!");
                displaySingleTaskTable(taskService.getTaskById(id));
            }
            else {
                System.out.println("❌ Failed to update task.");
            }
        }
        catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID. Please enter a number.");
        }
    }
    
    private void handleDelete(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: delete <id>");
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            Task task = taskService.getTaskById(id);
            
            if (task == null) {
                System.out.println("❌ Task " + id + " not found.");
                return;
            }
            
            System.out.println("Task to delete:");
            displaySingleTaskTable(task);
            System.out.print("Are you sure? (y/n): ");

            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("y")||confirm.equals("yes")) {
                if (taskService.deleteTask(id)) {
                    System.out.println("✅ Task " + id + " deleted successfully!");
                } else {
                    System.out.println("❌ Failed to delete task.");
                }
            }
            else {
                System.out.println("❌ Delete cancelled.");
            }
        }
        catch (NumberFormatException e) {
            System.out.println("❌ Invalid task ID. Please enter a number.");
        }
    }
    
    private void handleSearch(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: search <term>");
            return;
        }
        String searchTerm = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        List<Task> tasks = taskService.searchTasksByTitle(searchTerm);
        if (tasks.isEmpty()) {
            System.out.println("📝 No tasks found matching: " + searchTerm);
        }
        else {
            System.out.println("\n🔍 SEARCH RESULTS for '" + searchTerm + "':");
            displayTasksTable(tasks);
            System.out.println("Found: " + tasks.size() + " tasks");
        }
    }
    
    private void handleCategory(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: category <name>");
            return;
        }
        
        String categoryName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        List<Task> tasks = taskService.getTasksByCategory(categoryName);
        
        if (tasks.isEmpty()) {
            System.out.println("📝 No tasks found in category: " + categoryName);
        }
        else {
            System.out.println("\n📋 TASKS IN CATEGORY '" + categoryName + "':");
            displayTasksTable(tasks);
            System.out.println("Total: " + tasks.size() + " tasks");
        }
    }
    
    private void handleClear(String[] args) {
        if (args.length < 2) {
            System.out.println("❌ Usage: clear <completed|all>");
            return;
        }
        String type = args[1].toLowerCase();
        switch (type) {
            case "completed":
                System.out.print("Remove all completed tasks? (y/n): ");
                String confirm1 = scanner.nextLine().trim().toLowerCase();
                if (confirm1.equals("y")||confirm1.equals("yes")) {
                    int removed = taskService.clearCompletedTasks();
                    System.out.println(removed > 0 ? "✅ Cleared " + removed + " completed tasks!" : "📝 No completed tasks to clear.");
                } else {
                    System.out.println("❌ Operation cancelled.");
                }
                break;  
            case "all":
                System.out.print("⚠️ Delete ALL tasks permanently? (y/n): ");
                String confirm2 = scanner.nextLine().trim().toLowerCase();
                if (confirm2.equals("y")||confirm2.equals("yes")) {
                    int removed = taskService.clearAllTasks();
                    System.out.println("✅ Cleared all " + removed + " tasks!");
                } else {
                    System.out.println("❌ Operation cancelled.");
                }
                break;
            default:
                System.out.println("❌ Usage: clear <completed|all>");
        }
    }
    
    private void showStats() {
        if (taskService.hasTasks()) {
            List<Task> allTasks = taskService.getAllTasks();
            int total = allTasks.size();
            int completed = (int) allTasks.stream().filter(Task::isCompleted).count();
            int pending = total - completed;
            int overdue = (int) allTasks.stream().filter(t -> !t.isCompleted() && t.isOverdue()).count();
            System.out.println("\n📊 TASK STATISTICS:");
            System.out.println("Total tasks:     " + total);
            System.out.println("✅ Completed:    " + completed);
            System.out.println("📋 Pending:      " + pending);
            System.out.println("⏰ Overdue:      " + overdue);
        }
        else {
            System.out.println("\n📝 No tasks yet. Use 'add \"<title>\"' to get started!");
        }
    }
    
    // Display methods
    private void displayTasksTable(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("📝 No tasks to display");
            return;
        }
        System.out.println(TABLE_BORDER);
        System.out.println(TABLE_HEADER);
        System.out.println(TABLE_BORDER);
        for (Task task : tasks) {
            displayTaskRow(task);
        }
        System.out.println(TABLE_BORDER);
    }
    
    private void displaySingleTaskTable(Task task) {
        System.out.println(TABLE_BORDER);
        System.out.println(TABLE_HEADER);
        System.out.println(TABLE_BORDER);
        displayTaskRow(task);
        System.out.println(TABLE_BORDER);
    }
    
    private void displayTaskRow(Task task) {
        String id = String.format("%-4d", task.getId());
        String title = truncateString(task.getTitle(), 25);
        String category = truncateString(task.getCategory() != null ? task.getCategory() : "None", 13);
        String priority = String.format("%-8s", task.getPriority());
        String status = task.isCompleted() ? "COMPLETED " : "PENDING   ";
        String dueDate = formatDueDateForTable(task.getDueDate());
        String completed = task.isCompleted() ? "✅   " : "❌   ";
        System.out.printf("| %s | %s | %s | %s | %s | %s | %s |\n",
                id, title, category, priority, status, dueDate, completed);
    }
    
    private void showTaskDetails(Task task) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📋 TASK DETAILS");
        System.out.println("=".repeat(50));
        System.out.println("ID:          " + task.getId());
        System.out.println("Title:       " + task.getTitle());
        System.out.println("Description: " + (task.getDescription() != null && !task.getDescription().isEmpty() 
                                                ? task.getDescription() : "No description"));
        System.out.println("Category:    " + (task.getCategory() != null ? task.getCategory() : "None"));
        System.out.println("Priority:    " + task.getPriority());
        System.out.println("Status:      " + (task.isCompleted() ? "✅ COMPLETED" : "📋 PENDING"));
        System.out.println("Due Date:    " + formatDateTimeForDisplay(task.getDueDate()));
        System.out.println("Created:     " + formatDateTimeForDisplay(task.getCreatedAt()));
        System.out.println("=".repeat(50));
    }
    
    // Helper methods
    private String truncateString(String str, int maxLength) {
        if (str == null) str = "";
        if (str.length() <= maxLength) {
            return String.format("%-" + maxLength + "s", str);
        }
        else {
            return str.substring(0, maxLength - 3) + "...";
        }
    }
    
    private String formatDueDateForTable(String dueDate) {
        if (dueDate == null||dueDate.isEmpty()) {
            return String.format("%-16s", "No due date");
        }
        
        try {
            LocalDateTime dateTime;
            if (dueDate.contains("T")) {
                dateTime = LocalDateTime.parse(dueDate);
            }
            else {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                dateTime = LocalDateTime.parse(dueDate, inputFormatter);
            }
            
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
            String formatted = dateTime.format(outputFormatter);
            return String.format("%-16s", formatted);
        }
        catch (DateTimeParseException e) {
            return String.format("%-16s", dueDate.length() > 16 ? dueDate.substring(0, 16) : dueDate);
        }
    }
    
    private String formatDateTimeForDisplay(String dateTimeString) {
        if (dateTimeString == null||dateTimeString.isEmpty()) {
            return "Not set";
        }
        try {
            LocalDateTime dateTime;
            if (dateTimeString.contains("T")) {
                dateTime = LocalDateTime.parse(dateTimeString);
            }
            else {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);
            }
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            return dateTime.format(displayFormatter);
            
        }
        catch (DateTimeParseException e) {
            return dateTimeString;
        }
    }
}