package com.courseregist.course.DTO;

import com.courseregist.course.entity.HocKy;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HocKyRowMapper implements RowMapper<HocKy> {
    @Override
    public HocKy mapRow(ResultSet rs, int rowNum) throws SQLException {

        HocKy hk = new HocKy();
        hk.setMaHK(rs.getString("MaHK"));
        hk.setTenHK(rs.getString("TenHK"));
        hk.setNamHoc(rs.getString("NamHoc"));
        java.sql.Date ngayBatDau = rs.getDate("NgayBatDau");
        hk.setNgayBatDau(ngayBatDau);
        java.sql.Date ngayKetThuc = rs.getDate("NgayKetThuc");
        hk.setNgayKetThuc(ngayKetThuc);
        return hk;

    }
}
