package com.courseregist.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class user {
    @Id
    private String id;
    private String password;

    private String role; // "STUDENT", "LECTURER", "ADMIN"

    public String getId() {
        return id;
    }
    public void setUsername(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }


    public void setId(String id) { this.id = id; }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
