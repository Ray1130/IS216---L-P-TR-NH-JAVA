package com.courseregist.course.controller;

import com.courseregist.course.auth.SignInDto;
import com.courseregist.course.service.TokenProvider;
import com.courseregist.course.auth.JwtDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/login")
    public String login() {
        return "/login";
    }

    @PostMapping("/signin")
    @ResponseBody
    public ResponseEntity<JwtDto> signIn(@RequestBody @Valid SignInDto data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getUsername(), data.getPassword());
        Authentication authUser = authenticationManager.authenticate(usernamePassword); // xác thực username password
        String accessToken = tokenProvider.generateAccessToken(authUser.getName()); // tao jwt acess
        return ResponseEntity.ok(new JwtDto(accessToken));// tạo 1 tokenn và trả về token cho người dùng
    }

}