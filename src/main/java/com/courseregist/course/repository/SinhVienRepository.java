package com.courseregist.course.repository;

import com.courseregist.course.entity.SinhVien;
import com.courseregist.course.DTO.SinhVienRowMapper;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SinhVienRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @Autowired
    public SinhVienRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public List<SinhVien> getDanhSachSV() {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("DanhSach_SinhVien")
                    .declareParameters(
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new SinhVienRowMapper()));

            Map<String, Object> result = jdbcCall.execute();
            return (List<SinhVien>) result.get("p_cursor");
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Thêm sinh viên, kiểm tra MaHe, MaNganh và MaSV trước khi thêm
    public boolean addSinhVien(SinhVien sinhVien) {
        String checkMaHeSql = "SELECT COUNT(*) FROM CTDT WHERE TRIM(MaHe) = ?";
        String checkMaNganhSql = "SELECT COUNT(*) FROM NGANH WHERE TRIM(MaNganh) = ?";
        String checkMaSVSql = "SELECT COUNT(*) FROM SINHVIEN WHERE TRIM(MaSV) = ?";

        try (Connection connection = dataSource.getConnection()) {
            // Kiểm tra MaHe
            try (PreparedStatement checkHeStmt = connection.prepareStatement(checkMaHeSql)) {
                checkHeStmt.setString(1, sinhVien.getMaHe());
                ResultSet rs = checkHeStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.err.println("MaHe không tồn tại: " + sinhVien.getMaHe());
                    return false;
                }
            }

            // Kiểm tra MaNganh
            try (PreparedStatement checkNganhStmt = connection.prepareStatement(checkMaNganhSql)) {
                checkNganhStmt.setString(1, sinhVien.getMaNganh());
                ResultSet rs = checkNganhStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.err.println("MaNganh không tồn tại: " + sinhVien.getMaNganh());
                    return false;
                }
            }

            // kiểm tra Masv
            try (PreparedStatement checkMaSVStmt = connection.prepareStatement(checkMaSVSql)) {
                checkMaSVStmt.setString(1, sinhVien.getMaSV());
                ResultSet rs = checkMaSVStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Mã sinh viên đã tồn tại: " + sinhVien.getMaSV());
                    return false;
                }
            }
            // Nếu tất cả kiểm tra đều thành công, thực hiện thêm sinh viên, khi mới thêm
            // thì tình trạng là "Đang học"
            String sql = "INSERT INTO SinhVien(MaSV, HoSV, TenSV, GioiTinh, NgaySinh, TinhTrang, MaHe, MaNganh) VALUES (?, ?, ?, ?,?,'Đang học', ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, sinhVien.getMaSV());
                stmt.setString(2, sinhVien.getHoSV());
                stmt.setString(3, sinhVien.getTenSV());
                stmt.setString(4, sinhVien.getGioiTinh());
                stmt.setDate(5, new java.sql.Date(sinhVien.getNgaySinh().getTime()));
                stmt.setString(6, sinhVien.getMaHe());
                stmt.setString(7, sinhVien.getMaNganh());
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm sinh viên: " + e.getMessage());
            if (e instanceof SQLException) {
                System.err.println("SQL Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("SQL State: " + ((SQLException) e).getSQLState());
            }
            System.err.println("Message: " + e.getMessage());

            e.printStackTrace();
            return false;
        }
    }

    // Xóa sinh viên, kiểm tra MaSV trước khi xóa
    public boolean deleteSinhVien(SinhVien SinhVien) {
        String sql = "DELETE FROM SinhVien WHERE MaSV = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, SinhVien.getMaSV());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật thông tin sinh viên
    public boolean updateSinhVien(SinhVien svUpdate) {
        String sql = "UPDATE SinhVien SET HoSV = ?, TenSV = ?, GioiTinh = ? , NgaySinh = ?,  TinhTrang = ?, MaHe =? , MaNganh =?  WHERE MaSV = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, svUpdate.getHoSV());
            stmt.setString(2, svUpdate.getTenSV());
            stmt.setString(3, svUpdate.getGioiTinh());
            stmt.setDate(4, new java.sql.Date(svUpdate.getNgaySinh().getTime()));
            stmt.setString(5, svUpdate.getTinhTrang());
            stmt.setString(6, svUpdate.getMaHe());
            stmt.setString(7, svUpdate.getMaNganh());
            stmt.setString(8, svUpdate.getMaSV()); // Đặt MaSV ở cuối để xác định bản ghi cần cập nhật

            System.out.println(svUpdate.getMaSV());
            System.out.println(svUpdate.getHoSV());
            System.out.println(svUpdate.getTenSV());
            System.out.println(svUpdate.getGioiTinh());

            System.out.println(stmt.executeUpdate() > 0);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tìm sinh viên theo mã, sử dụng TRIM để loại bỏ khoảng trắng
    public SinhVien findByIdSV(String MaSV) {
        String sql = "SELECT * FROM SinhVien WHERE TRIM(MASV) = ?"; //
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, MaSV);
            ResultSet rs = stmt.executeQuery();

            // Kiểm tra xem có kết quả trả về không
            if (rs.next()) {
                SinhVien sv = new SinhVien();
                sv.setMaSV(rs.getString("maSV"));
                sv.setHoSV(rs.getString("hoSV"));
                sv.setTenSV(rs.getString("tenSV"));
                sv.setGioiTinh(rs.getString("gioiTinh"));
                java.sql.Date ngaySinhDate = rs.getDate("ngaySinh");
                if (ngaySinhDate != null) {
                    sv.setNgaySinh(ngaySinhDate);
                }
                sv.setTinhTrang(rs.getString("tinhTrang"));
                sv.setMaHe(rs.getString("maHe"));
                sv.setMaNganh(rs.getString("maNganh"));
                System.out.println("Tìm thấy sinh viên mã " + MaSV);
                return sv;
            } else {
                System.out.println("Không tìm thấy sinh viên với mã: " + MaSV);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn findById:");
            e.printStackTrace();
        }
        return null;
    }

    public List<SinhVien> searchSinhVien(String maSV, String hoTenSearch) { // Thay thế hoTenSV bằng hoTenSearch để tìm
                                                                            // kiếm chung
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("Search_SinhVien")
                    .declareParameters(
                            new SqlParameter("p_maSV", OracleTypes.VARCHAR),
                            new SqlParameter("p_hoTen_search", OracleTypes.VARCHAR),
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new SinhVienRowMapper()));

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_maSV", maSV);
            inParams.put("p_hoTen_search", hoTenSearch);// Sử dụng hoTenSearch để tìm kiếm chung

            Map<String, Object> result = jdbcCall.execute(inParams);

            System.out.println("Kết quả trả về từ procedure: " + result);

            return (List<SinhVien>) result.get("p_cursor");

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi procedure Search_SinhVien: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Trả về danh sách rỗng nếu có lỗi
        }
    }

}