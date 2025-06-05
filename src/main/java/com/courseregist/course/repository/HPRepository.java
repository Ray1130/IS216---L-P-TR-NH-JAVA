package com.courseregist.course.repository;

import com.courseregist.course.DTO.HPDTO;
import com.courseregist.course.DTO.HPDTORowMapper;
import com.courseregist.course.DTO.SinhVienRowMapper;
import com.courseregist.course.entity.SinhVien;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OracleTypes;

@Repository
public class HPRepository {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // cái này chỉ hiển thị ở kì này trong giao diện dkhp
    public List<HPDTO> getDanhSachHP() {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("HienThi_DanhSach") // hiển thị danh sách học phần ở kì này
                .declareParameters(
                        new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new HPDTORowMapper())); // ánh xạ với
                                                                                                    // database và trả
                                                                                                    // về các dữ liệu

        Map<String, Object> result = jdbcCall.execute();
        return (List<HPDTO>) result.get("p_cursor"); // con trỏ trỏ tới các hàm
    }

    // cái này hiện tất cả học phần thêm vào dtb
    public List<HPDTO> getAllDanhSachHP() {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("DanhSach_LopHoc")
                .declareParameters(
                        new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new HPDTORowMapper()));

        Map<String, Object> result = jdbcCall.execute();
        return (List<HPDTO>) result.get("p_cursor");
    }
    // danh sách đăng kí của sinh viên

    public List<HPDTO> getDanhSachHPDK(String maSV) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("HienThi_DanhSachDangKy")
                .declareParameters(
                        new SqlParameter("p_masv", OracleTypes.VARCHAR),
                        new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new HPDTORowMapper()));

        Map<String, Object> inParams = new HashMap<>();// map các tham số
        inParams.put("p_masv", maSV == null ? "" : maSV); // kiểm tra mã sinh viên null hay không
        Map<String, Object> result = jdbcCall.execute(inParams); // kết quả trả về là một map được gọi từ jdbc với các
                                                                 // tham số inParams
        return (List<HPDTO>) result.get("p_cursor");
    }

    // tìm kiếm theo mã lớp, tên môn học
    public List<HPDTO> searchHP(String maLop, String tenMH) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("Search_HP")
                    .declareParameters(
                            new SqlParameter("p_maLop", OracleTypes.VARCHAR),
                            new SqlParameter("p_tenMH", OracleTypes.VARCHAR),
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new HPDTORowMapper()));

            Map<String, Object> inParams = new HashMap<>();// một map các tham số
            inParams.put("p_maLop", maLop == null ? "" : maLop); // kiểm tra null
            inParams.put("p_tenMH", tenMH == null ? "" : tenMH);
            Map<String, Object> result = jdbcCall.execute(inParams);
            return (List<HPDTO>) result.get("p_cursor");

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi procedure Search_HP: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    // lấy danh sách đăng kí học phần theo học kì, năm học

    public List<HPDTO> layDanhSachTuHK(String maSV, String tenHK, String namHoc) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("Search_Phieu")
                    .declareParameters(
                            new SqlParameter("p_maSV", OracleTypes.VARCHAR),
                            new SqlParameter("p_tenHK", OracleTypes.VARCHAR),
                            new SqlParameter("p_namHoc", OracleTypes.VARCHAR),
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new HPDTORowMapper()));

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_maSV", maSV == null ? "" : maSV);
            inParams.put("p_tenHK", tenHK == null ? "" : tenHK);
            inParams.put("p_namHoc", namHoc == null ? "" : namHoc);
            Map<String, Object> result = jdbcCall.execute(inParams);
            return (List<HPDTO>) result.get("p_cursor");

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi procedure Search_HP: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // xóa các học phần đã đăng kí
    public boolean xoaDangKyHocPhan(String maSV, String maLop) {
        String sql = "DELETE FROM CHITIETDK WHERE MaPhieuDKHP IN (SELECT MaPhieuDKHP FROM PHIEUDKHP WHERE TRIM(MaSV) = ?) AND TRIM(MaLop) = TRIM(?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, maSV);
            stmt.setString(2, maLop);
            System.out.println("Gửi yêu cầu xóa lớp: " + maLop + " của sinh viên: " + maSV);
            int affectedRows = stmt.executeUpdate();
            System.out.println(
                    "DELETE FROM CHITIETDK WHERE MaPhieuDKHP IN (SELECT MaPhieuDKHP FROM PHIEUDKHP WHERE MaSV = '"
                            + maSV + "') AND TRIM(MaLop) = TRIM('" + maLop + "')");
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // lấy danh sách sinh viên đăng ký theo lớp

    public List<SinhVien> getSinhVienDangKy(String maLop) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("DanhSach_SinhVienDangKyLop")
                    .declareParameters(
                            new SqlParameter("p_maLop", OracleTypes.VARCHAR),
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new SinhVienRowMapper()));

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_maLop", maLop == null ? "" : maLop);
            Map<String, Object> result = jdbcCall.execute(inParams);
            return (List<SinhVien>) result.get("p_cursor");

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi procedure Search_HP: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

}
