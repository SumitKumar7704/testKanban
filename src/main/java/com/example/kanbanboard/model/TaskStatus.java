package com.example.kanbanboard.model;

public enum TaskStatus {
    TODO, //to be performed
    IN_PROGRESS, //currently in progress(maxed to 3 with WIP implemented with exeption)
    DONE //tasked marked done later to be confirmed by the admin
}

