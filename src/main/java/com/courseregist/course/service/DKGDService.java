package com.courseregist.course.service;

import com.courseregist.course.DTO.LichDangKyDTO;
import com.courseregist.course.entity.MonHoc;
import com.courseregist.course.repository.HPRepository;
import com.courseregist.course.repository.LichDayRepository;
import com.courseregist.course.repository.MonHocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.courseregist.course.entity.GiangVien;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DKGDService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @Autowired
    public DKGDService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Autowired
    private MonHocRepository monHocRepository;

    @Autowired
    private HPRepository hpRepository;

    private final LichDangKyDTO lichDangKyDTO = new LichDangKyDTO();

    @Autowired
    private LichDayRepository lichDayRepository;

    GiangVien giangVien = new GiangVien();

    public List<MonHoc> getDanhSachMH() { return monHocRepository.getDanhSachMH(); }

    public List<MonHoc> getDanhSachMHDaDangKy(String maGV) { return monHocRepository.getDanhSachMHDaDangKy(maGV); }

    public List<LichDangKyDTO> getDanhSachLDDaDangKy(String maGV) { return lichDayRepository.getDanhSachLDDaDangKy(maGV); }

    public List<MonHoc> searchMonHoc(String maMH, String tenMH) {
        String maMHTrimmed = (maMH != null) ? maMH.trim() : "";
        String tenMHTrimmed = (tenMH != null) ? tenMH.trim() : "";

        if(maMHTrimmed.isEmpty() && tenMHTrimmed.isEmpty()) {
            return monHocRepository.getDanhSachMH();
        }

        return monHocRepository.searchMonHoc(maMHTrimmed, tenMHTrimmed);
    }

    public List<MonHoc> searchMonHoc(String maGV, String tenHK, String namHoc) {
        String maGVTrimmed = (maGV != null) ? maGV.trim() : "";
        String tenHKTrimmed = (tenHK != null) ? tenHK.trim() : "";
        String namHocTrimmed = (namHoc != null) ? namHoc.trim() : "";

        if (maGVTrimmed.isEmpty() && tenHKTrimmed.isEmpty() && namHocTrimmed.isEmpty()) {
            return monHocRepository.getDanhSachMH();
        }

        return monHocRepository.searchMonHoc(maGVTrimmed, tenHKTrimmed, namHocTrimmed);
    }

    public String getMaHKMoiNhat() {
        return hpRepository.getMaHKMoiNhat(); 
    }

    public int dangKyMonDay(String maGV, String maHK, String maMH) {
        // debug
        System.out.println("MaGV: [" + maGV + "]");
        System.out.println("Length: " + maGV.length());

        System.out.println("MaHK: [" + maHK + "]");
        System.out.println("Length: " + maHK.length());

        System.out.println("MaMH: [" + maMH + "]");
        System.out.println("Length: " + maMH.length());

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate) 
                .withProcedureName("DANGKY_MONDAY") 
                .declareParameters( 
                        new SqlParameter("p_magv", Types.VARCHAR), 
                        new SqlParameter("p_mahk", Types.VARCHAR),
                        new SqlParameter("p_mamh", Types.VARCHAR),
                        new SqlOutParameter("p_result", Types.INTEGER));

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_magv", maGV);
        inParams.put("p_mahk", maHK);
        inParams.put("p_mamh", maMH);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (Integer) result.get("p_result");
    }

    public int dangKyLichDay(String maGV, String maHK, String lichDay) {
        
        System.out.println("MaGV: [" + maGV + "]");
        System.out.println("Length: " + maGV.length());

        System.out.println("MaHK: [" + maHK + "]");
        System.out.println("Length: " + maHK.length());

        System.out.println("Danh sách lịch dạy: [Thứ " + lichDay.substring(0, 1) + ", Tiết " + lichDay.substring(2) + "]");

        String malichDay = lichDangKyDTO.convertToMaLichDay(lichDay);

        System.out.println("Mã lịch dạy sau khi đã convert: " + malichDay);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("DANGKY_LICHDAY")
                .declareParameters(
                        new SqlParameter("p_magv", Types.VARCHAR),
                        new SqlParameter("p_mahk", Types.VARCHAR),
                        new SqlParameter("p_malichday", Types.VARCHAR),
                        new SqlOutParameter("p_result", Types.INTEGER)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_magv", maGV);
        inParams.put("p_mahk", maHK);
        inParams.put("p_malichday", malichDay);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (Integer) result.get("p_result");
    }

    public boolean deleteMonHoc_GV(String maMH) {
        if(maMH == null || maMH.isEmpty()) {
            return false;
        }
        return monHocRepository.deleteMonHoc_GV(maMH);
    }
    
    public boolean deleteTietDay(String maGV, String thu) {
        
        String trichxuat = thu.substring(4);
        int thuInt = Integer.parseInt(trichxuat);
        System.out.println("Thứ sẽ xóa trong database: "+ thuInt);

        String sql = "DELETE FROM DANGKYLICH DK WHERE EXISTS " +
                "(SELECT 1 FROM PHIEUDKDAY PD JOIN LICHDAY LD ON LD.MALICHDAY = DK.MALICHDAY " +
                "WHERE PD.MAPHIEUDKDAY = DK.MAPHIEUDKDAY AND TRIM(PD.MAGV) = TRIM(?) AND LD.THU = ?)";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maGV);
            stmt.setInt(2, thuInt);
            System.out.println("UPDATE THANH CONG: maGV " + maGV + " thu " + thuInt);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
