package com.example.kanbanboard.model;

import java.util.ArrayList;
import java.util.List;

public class Column {

    private String id;
    private String name;


    private List<Task> tasks = new ArrayList<>();

    public Column() {
    }

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
