package com.courseregist.course.auth;

import jakarta.validation.constraints.NotBlank;

public class SignInDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // Getter và Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
