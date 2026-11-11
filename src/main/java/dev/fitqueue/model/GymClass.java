package dev.fitqueue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.Instant;

@Entity
@Table(name = "gym_classes")
public class GymClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    protected GymClass() {
    }

    public GymClass(Long id, String title, int capacity, Instant startsAt){
        this.id = id;
        this.title = title;
        this.capacity = capacity;
        this.startsAt = startsAt;
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

    public Instant getStartsAt() {
        return startsAt;
    }
}