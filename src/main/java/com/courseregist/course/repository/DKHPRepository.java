package com.courseregist.course.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.courseregist.course.entity.DongMoDKHP;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public class DKHPRepository {

    private JdbcTemplate jdbcTemplate;

    public DKHPRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // xem trạng thái đóng mở của các học phần đăng ký theo học kỳ
    public DongMoDKHP getTrangThaiDK(String maHK) {
        String sql = "SELECT MaDK, MaHK, TrangThai, NgayBatDau, NgayKetThuc FROM DONGMO_DKHP WHERE MaHK = ?";
        return jdbcTemplate.queryForObject(sql, new Object[] { maHK }, (rs, rowNum) -> new DongMoDKHP(
                rs.getInt("MaDK"),
                rs.getString("MaHK"),
                rs.getInt("TrangThai") == 1,
                rs.getTimestamp("NgayBatDau").toLocalDateTime(),
                rs.getTimestamp("NgayKetThuc").toLocalDateTime()));
    }

    // kiểm tra xem học kì nào còn đang mở

    public List<DongMoDKHP> danhSachDangHoatDong() {
        String sql = "SELECT MaDK, MaHK, TrangThai, NgayBatDau, NgayKetThuc FROM DONGMO_DKHP WHERE TrangThai = 1";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DongMoDKHP(
                rs.getInt("MaDK"),
                rs.getString("MaHK"),
                rs.getInt("TrangThai") == 1,
                rs.getTimestamp("NgayBatDau").toLocalDateTime(),
                rs.getTimestamp("NgayKetThuc").toLocalDateTime()));
    }

    // cập nhật trạng thái của trang dkhp
    public void updateTrangThaiDK(String maHK, boolean trangThai, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "UPDATE DONGMO_DKHP SET TrangThai = ?, NgayBatDau = ?, NgayKetThuc = ? WHERE MaHK = ?";
        jdbcTemplate.update(sql, trangThai ? 1 : 0, startDate, endDate, maHK);
    }

    // thêm kì đkhp
    public void insertRegistration(String maHK, boolean isOpen, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "INSERT INTO DONGMO_DKHP (MaDK, MaHK, TrangThai, NgayBatDau, NgayKetThuc) VALUES (DongMoDKHP_SEQ.NEXTVAL, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, maHK, isOpen ? 1 : 0, startDate, endDate);

    }

    // kiểm tra mhk đã được set chưa
    public boolean checkMaHKExists(String maHK) {
        String sql = "SELECT COUNT(*) FROM DONGMO_DKHP WHERE Trim(MaHK) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[] { maHK }, Integer.class);
        return count != null && count > 0;
    }

}
