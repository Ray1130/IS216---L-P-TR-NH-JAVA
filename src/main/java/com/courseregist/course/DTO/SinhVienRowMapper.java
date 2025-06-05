package com.courseregist.course.DTO;

import com.courseregist.course.entity.SinhVien;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SinhVienRowMapper implements RowMapper<SinhVien> {
    @Override
    public SinhVien mapRow(ResultSet rs, int rowNum) throws SQLException {

        SinhVien sv = new SinhVien();
        sv.setMaSV(rs.getString("MaSV"));
        sv.setHoTen(rs.getString("HoTen"));
        sv.setGioiTinh(rs.getString("GioiTinh"));
        java.sql.Date ngaySinhDate = rs.getDate("NgaySinh");
        sv.setNgaySinh(ngaySinhDate);
        sv.setTinhTrang(rs.getString("TinhTrang"));
        sv.setMaHe(rs.getString("MaHe"));
        sv.setMaNganh(rs.getString("MaNganh"));

        return sv;

    }
}
