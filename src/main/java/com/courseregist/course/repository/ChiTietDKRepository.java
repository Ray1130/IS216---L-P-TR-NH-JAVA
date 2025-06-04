package com.courseregist.course.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;

@Repository
public class ChiTietDKRepository {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // đểm số sinh viên đăng kí theo lớp
    public int siSoDangKyHP(String maLop) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CHITIETDK WHERE MALOP = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, maLop);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0; // Trường hợp không tìm thấy dữ liệu
    }

}
