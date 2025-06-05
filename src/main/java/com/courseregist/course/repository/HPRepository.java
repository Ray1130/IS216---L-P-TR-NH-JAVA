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

    // thêm lớp học
    public boolean addLopHoc(HPDTO lh) {
        String checkSql = "SELECT COUNT(*) FROM LopHoc WHERE TRIM(MaLop) = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, lh.getMaLop());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Đã tồn tại lớp học
            }

            String sql = "INSERT INTO LopHoc (MaLop, SiSo, ThuNgayHoc, NgayBatDau, NgayKetThuc, CachTuan, TietHoc, " +
                    "NgonNguGiangDay, MaGV, MaMH, MaHK, PhongHoc, SoTinChi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, lh.getMaLop());
                stmt.setInt(2, lh.getSiSo());
                stmt.setInt(3, lh.getThuNgayHoc());
                stmt.setDate(4, new java.sql.Date(lh.getNgayBatDau().getTime()));
                stmt.setDate(5, new java.sql.Date(lh.getNgayKetThuc().getTime()));
                stmt.setInt(6, lh.getCachTuan());
                stmt.setString(7, lh.getTietHoc());
                stmt.setString(8, lh.getNgonNguGiangDay());
                stmt.setString(9, lh.getMaGV());
                stmt.setString(10, lh.getMaMH());
                stmt.setString(11, lh.getMaHK());
                stmt.setString(12, lh.getPhongHoc());
                stmt.setInt(13, lh.getSoTinChi());

                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // xóa
    public boolean deleteLopHoc(HPDTO lopHoc) {
        String sql = "DELETE FROM LopHoc WHERE MaLop = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lopHoc.getMaLop());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLopHoc(HPDTO lh) {
        String sql = "UPDATE LopHoc SET SiSo = ?, ThuNgayHoc = ?, NgayBatDau = ?, NgayKetThuc = ?, CachTuan = ?, TietHoc = ?, "
                +
                "NgonNguGiangDay = ?, MaGV = ?, MaMH = ?, MaHK = ?,  SoTinChi = ?, PhongHoc = ? WHERE MaLop = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lh.getSiSo());
            stmt.setInt(2, lh.getThuNgayHoc());
            stmt.setDate(3, new java.sql.Date(lh.getNgayBatDau().getTime()));
            stmt.setDate(4, new java.sql.Date(lh.getNgayKetThuc().getTime()));
            stmt.setInt(5, lh.getCachTuan());
            stmt.setString(6, lh.getTietHoc());
            stmt.setString(7, lh.getNgonNguGiangDay());
            stmt.setString(8, lh.getMaGV());
            stmt.setString(9, lh.getMaMH());
            stmt.setString(10, lh.getMaHK());
            stmt.setInt(11, lh.getSoTinChi());
            stmt.setString(12, lh.getPhongHoc());
            stmt.setString(13, lh.getMaLop());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // tìm lophoc
    public HPDTO findLopHocById(String maLop) {
        String sql = "SELECT * FROM LopHoc WHERE TRIM(MaLop) = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maLop);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                HPDTO lh = new HPDTO();
                lh.setMaLop(rs.getString("MaLop"));
                lh.setSiSo(rs.getInt("SiSo"));
                lh.setThuNgayHoc(rs.getInt("ThuNgayHoc"));
                lh.setNgayBatDau(rs.getDate("NgayBatDau"));
                lh.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                lh.setCachTuan(rs.getInt("CachTuan"));
                lh.setTietHoc(rs.getString("TietHoc"));
                lh.setNgonNguGiangDay(rs.getString("NgonNguGiangDay"));
                lh.setMaGV(rs.getString("MaGV"));
                lh.setMaMH(rs.getString("MaMH"));
                lh.setMaHK(rs.getString("MaHK"));
                lh.setPhongHoc(rs.getString("PhongHoc"));
                lh.setSoTinChi(rs.getInt("SoTinChi"));
                return lh;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // tìm kiếm xem trong danh sách lớp học
    public List<HPDTO> searchLopHoc(String maLop, String tenMH, String tenHK, String namHoc) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("Search_Lop")
                    .declareParameters(
                            new SqlParameter("p_maLop", OracleTypes.VARCHAR),
                            new SqlParameter("p_tenMH", OracleTypes.VARCHAR),
                            new SqlParameter("p_tenHK", OracleTypes.VARCHAR),
                            new SqlParameter("p_namHoc", OracleTypes.VARCHAR),
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new HPDTORowMapper()));

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_maLop", maLop == null ? "" : maLop);
            inParams.put("p_tenMH", tenMH == null ? "" : tenMH);
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

}
