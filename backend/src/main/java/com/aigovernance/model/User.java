package com.aigovernance.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="users")
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(unique=true,nullable=false) public String username;
 @Column(name="password_hash",nullable=false) public String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false) public Role role;
 public String department;
 @Column(name="created_at") public Instant createdAt=Instant.now();
}
