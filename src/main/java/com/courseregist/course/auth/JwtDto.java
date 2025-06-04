//truyền jwtdto về client
package com.courseregist.course.auth;

;

public class JwtDto {
    private String token;

    public JwtDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}