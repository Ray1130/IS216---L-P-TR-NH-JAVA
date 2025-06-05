package com.courseregist.course.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;
import java.sql.*;
import java.util.List;

@Repository
public class AdminDashBoardRepository {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> laySinhVienTheoNganh() {
        String sql = "SELECT MaNganh, COUNT(DISTINCT MaSV) AS so_luong_sinh_vien FROM SINHVIEN GROUP BY MaNganh";
        return jdbcTemplate.queryForList(sql);
    }

    public Integer soLuongSinhVienDKHP() {
        String sql = "SELECT COALESCE(COUNT(DISTINCT p.MaSV), 0) as SOSINHVIEN_HIENTAI " +
                "FROM HOCKY hk " +
                "LEFT JOIN PHIEUDKHP p ON hk.MaHK = p.MaHK " +
                "WHERE CURRENT_DATE BETWEEN hk.NgayBatDau AND hk.NgayKetThuc";
        try {
            Integer soLuong = jdbcTemplate.queryForObject(sql, Integer.class);

            // queryForObject sẽ ném EmptyResultDataAccessException nếu query không trả về
            return (soLuong != null) ? soLuong : 0;

        } catch (EmptyResultDataAccessException e) {
            // Xảy ra khi không có HOCKY nào khớp với điều kiện ngày hiện tại.
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Double trungBinhTinChi() {
        String sql = "SELECT AVG(TongSoTinChi) AS SOTINCHI_DK_TRUNGBINH\r\n" + //
                "FROM (\r\n" + //
                "    SELECT p.MaPhieuDKHP, SUM(SoTinChi) AS TongSoTinChi\r\n" + //
                "    FROM CHITIETDK ct\r\n" + //
                "    JOIN PHIEUDKHP p ON ct.MaPhieuDKHP = p.MaPhieuDKHP\r\n" + //
                "    JOIN HOCKY hk ON hk.MaHK = p.MaHK\r\n" + //
                "    JOIN LOPHOC l ON l.MaLop = ct.MaLop\r\n" + //
                "    WHERE CURRENT_DATE BETWEEN hk.NgayBatDau AND hk.NgayKetThuc\r\n" + //
                "    GROUP BY p.MaPhieuDKHP\r\n" + //
                ") sub";
        try {
            Double soLuong = jdbcTemplate.queryForObject(sql, Double.class);

            // queryForObject sẽ ném EmptyResultDataAccessException nếu query không trả về
            return (soLuong != null) ? soLuong : 0;

        } catch (

        EmptyResultDataAccessException e) {
            return 0.0;
        }
    }

    public Integer soLuongHocPhan() {
        String sql = "SELECT COALESCE(COUNT(DISTINCT l.MaLop), 0) as SOHOCPHANDAMO\r\n" + //
                "FROM LOPHOC l JOIN HOCKY hk ON l.MaHK = hk.MaHK\r\n" + //
                "WHERE CURRENT_DATE BETWEEN hk.NgayBatDau AND hk.NgayKetThuc";
        try {
            Integer soLuong = jdbcTemplate.queryForObject(sql, Integer.class);
            // queryForObject sẽ ném EmptyResultDataAccessException nếu query không trả về
            return (soLuong != null) ? soLuong : 0;

        } catch (EmptyResultDataAccessException e) {
            // Xảy ra khi không có HOCKY nào khớp với điều kiện ngày hiện tại.
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Map<String, Object>> theoDoiSoLuotDKTheoGio() {
        String sql = "SELECT TRUNC(ThoiGianDK, 'HH') AS gio_dang_ky_lop, " +
                "COUNT(MaLop) AS so_lop_duoc_dang_ky " +
                "FROM CHITIETDK " +
                "GROUP BY TRUNC(ThoiGianDK, 'HH') ORDER BY gio_dang_ky_lop ASC";

        return jdbcTemplate.queryForList(sql);
    }// 24h gần nhất

    public List<Map<String, Object>> getSoLuotDKTrong24GioGanNhat() {
        String sql = "SELECT TRUNC(ThoiGianDK, 'HH') AS gio_dang_ky_lop, " +
                "COUNT(MaLop) AS so_lop_duoc_dang_ky " +
                "FROM CHITIETDK " +
                "WHERE ThoiGianDK >= SYSDATE - INTERVAL '1' DAY " +
                "  AND ThoiGianDK <= SYSDATE " +
                "GROUP BY TRUNC(ThoiGianDK, 'HH') " +
                "ORDER BY gio_dang_ky_lop";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> theoDoiSoLuotDKTheoNgay() {
        String sql = "SELECT TRUNC(ThoiGianDK) AS ngay_dang_ky_lop, " +
                "COUNT(MaLop) AS so_luot_duoc_dang_ky " +
                "FROM CHITIETDK " +
                "GROUP BY TRUNC(ThoiGianDK)  ORDER BY ngay_dang_ky_lop";

        return jdbcTemplate.queryForList(sql);
    }

}
