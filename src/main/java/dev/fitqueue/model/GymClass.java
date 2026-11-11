package dev.fitqueue.model;

public class GymClass {

    private Long id;
    private String title;
    private int capacity;

    public GymClass(Long id, String title, int capacity){
        this.id = id;
        this.title = title;
        this.capacity = capacity;
    }

    public Long getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public int getCapacity(){
        return capacity;
    }
}