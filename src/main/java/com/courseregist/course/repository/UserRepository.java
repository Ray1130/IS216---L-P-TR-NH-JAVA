package com.courseregist.course.repository;

import com.courseregist.course.entity.user;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    public UserRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public user findByUsername(String username) {
        String sql = "SELECT * FROM USERS WHERE USERNAME = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user user = new user();
                user.setUsername(rs.getString("USERNAME"));
                user.setPassword(rs.getString("PASSWORD"));
                user.setRole(rs.getString("ROLE"));
                System.out.println("Tìm thấy USER:  " + username);
                return user;
            } else {
                System.out.println("Không tìm thấy USER: " + username);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn findById:");
            e.printStackTrace();
        }
        return null;
    }

    public void saveUser(String username, String newPassword) {
        String sql = "UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?";
        // Mã hóa mật khẩu mới trước khi lưu
        // String encodedPassword = passwordEncoder.encode(newPassword);

        int rowsAffected = jdbcTemplate.update(sql, newPassword, username);

        if (rowsAffected == 0) {
            throw new RuntimeException("User not found or update failed");
        }

    }

}
