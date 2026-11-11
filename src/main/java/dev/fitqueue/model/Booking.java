package dev.fitqueue.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;

@Entity 
@Table(name = "bookings")
public class Booking {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private GymClass gymClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Booking(){
    }

    public Booking(Long id, GymClass gymClass, Member member, BookingStatus status, Instant createdAt){
        this.id = id;
        this.gymClass = gymClass;
        this.member = member;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId(){
        return id;
    }

    public GymClass getGymClass(){
        return gymClass;
    }

    public Member getMember(){
        return member;
    }

    public BookingStatus getBookingStatus(){
        return status;
    }

    public Instant getCreatedAt(){
        return createdAt;
    }

    public void cancel(){
        this.status = BookingStatus.CANCELLED;
    }

    public void confirm(){
        if(this.status != BookingStatus.WAITLISTED){
            throw new IllegalStateException(
                "Only waitlisted bookings can be confirmed"
            );
        }

        this.status = BookingStatus.CONFIRMED;
    }

}
