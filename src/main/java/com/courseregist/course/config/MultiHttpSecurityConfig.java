package com.courseregist.course.config;

import com.courseregist.course.entity.user;
import com.courseregist.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // new import
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.courseregist.course.auth.CustomAuthenticationSuccessHandler;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
// cấu hình đăng nhập
public class MultiHttpSecurityConfig {

        @Autowired
        private TokenAuthenticationFilter tokenAuthenticationFilter; // sử dụng token để lấy thông tin đăng nhập
        @Autowired
        private UserDetailsService userDetailsService; // lấy từ database

        @Bean
        public AuthenticationSuccessHandler authenticationSuccessHandler() {
                return new SimpleUrlAuthenticationSuccessHandler("/");
        }

        @Autowired
        private CustomAuthenticationSuccessHandler successHandler;

        @Bean
        // cấp quyền
        public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/api/**")
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/api/v1/auth/**").permitAll() // <<< Cho phép
                                                // /api/v1/auth/**
                                                .anyRequest().hasRole("ADMIN"))
                                .csrf(csrf -> csrf.disable()) // tắt csrf
                                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .httpBasic(Customizer.withDefaults());

                return http.build();
        }

        @Bean
        public SecurityFilterChain formLoginFilterChain(HttpSecurity http, DataSource dataSource) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authorize -> authorize // cho phép đăng nhập những link sau
                                                .requestMatchers(
                                                                "/",
                                                                "/home",
                                                                "/login",
                                                                "/DKHP",
                                                                "/dashboarddkhp",
                                                                "/danhsachdkhp",
                                                                "/formdkhp",
                                                                "/DKGD",
                                                                "/dashboarddkgd",
                                                                "/styleDKGD",
                                                                "/forgot-password",
                                                                "/UIT1.jpg",
                                                                "/newlogo-removebg.png",
                                                                "/api/sheets/**",
                                                                "/css/**", "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/note/**").hasRole("STUDENT")
                                                .requestMatchers("/lecturer/**").hasRole("LECTURER") // phân role
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler(successHandler) // <<< Gắn SuccessHandler tự custom
                                                .failureUrl("/login?error") // login thất bại thì về lại /login?error
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout") // URL để gọi logout
                                                .logoutSuccessUrl("/login?logout") // Sau khi logout sẽ chuyển về đây
                                                .invalidateHttpSession(true) // Hủy session
                                                .clearAuthentication(true) // Xóa Authentication
                                                .deleteCookies("JSESSIONID") // Xóa cookies (nếu có)
                                                .permitAll())

                                .passwordManagement(Customizer.withDefaults()); // kích hoạt tính năng đổi mật khẩu tự
                // động

                return http.build();
        }

        // mã hóa mật khẩu
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager() {
                DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
                authenticationProvider.setUserDetailsService(userDetailsService);
                authenticationProvider.setPasswordEncoder(passwordEncoder()); // mã hóa mật khẩu

                ProviderManager providerManager = new ProviderManager(authenticationProvider);
                providerManager.setEraseCredentialsAfterAuthentication(false);
                return providerManager;
        }

}
