package dev.fitqueue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity 
@Table(name = "members")
public class Member {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role = MemberRole.MEMBER;
    
    protected Member(){
    }

    public Member(Long id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Member(
        Long id,
        String name,
        String email,
        String passwordHash){
            this.id = id;
            this.name = name;
            this.email = email;
            this.passwordHash = passwordHash;
        }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    @JsonIgnore 
    public String getPasswordHash() {
        return passwordHash;
    }

    public MemberRole getRole(){
        return role;
    }
}
