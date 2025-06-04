package com.courseregist.course.DTO;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HPDTORowMapper implements RowMapper<HPDTO> {
    // class rowmapper để ánh xạ từng dòng của resultset với database

    @Override
    public HPDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        HPDTO dto = new HPDTO();
        dto.setMaLop(rs.getString("MaLop"));
        dto.setSiSo(rs.getInt("SiSo"));
        dto.setThuNgayHoc(rs.getInt("ThuNgayHoc"));
        dto.setNgayBatDau(rs.getDate("NgayBatDau"));
        dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
        dto.setCachTuan(rs.getInt("CachTuan"));
        dto.setTietHoc(rs.getString("TietHoc"));
        dto.setNgonNguGiangDay(rs.getString("NgonNguGiangDay"));
        dto.setMaMH(rs.getString("MaMH"));
        dto.setTenMH(rs.getString("TenMH"));
        dto.setHoGV(rs.getString("HoGV"));
        dto.setTenGV(rs.getString("TenGV"));
        dto.setSoTinChi(rs.getInt("SoTinChi"));
        dto.setMaHK(rs.getString("MaHK"));
        dto.setPhongHoc(rs.getString("PhongHoc"));
        dto.setMaGV(rs.getString("MaGV"));

        return dto;

    }
}