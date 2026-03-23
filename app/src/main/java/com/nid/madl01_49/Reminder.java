package com.nid.madl01_49; // Keep your package name!

public class Reminder {
    int id; // Added ID
    String title, description, time, location, category;

    public Reminder(int id, String title, String description, String time, String location, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.time = time;
        this.location = location;
        this.category = category;
    }
}