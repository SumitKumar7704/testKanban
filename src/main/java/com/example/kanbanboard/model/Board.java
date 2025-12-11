package com.example.kanbanboard.model;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private String id;
    private String name;


    private List<Column> columns = new ArrayList<>();

    public Board() {
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

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
