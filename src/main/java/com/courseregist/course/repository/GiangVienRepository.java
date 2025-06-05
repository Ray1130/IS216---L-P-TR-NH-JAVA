package com.courseregist.course.repository;

import com.courseregist.course.entity.GiangVien;
import com.courseregist.course.DTO.GiangVienRowMapper;

import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
public class GiangVienRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @Autowired
    public GiangVienRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public List<GiangVien> getDanhSachGiangVien() {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("DanhSach_GiangVien")
                    .declareParameters(
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new GiangVienRowMapper()));

            Map<String, Object> result = jdbcCall.execute();
            return (List<GiangVien>) result.get("p_cursor");
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /*
     * public boolean addGiangVien(GiangVien gv) {
     * String sql =
     * "INSERT INTO GiangVien(maGV, hoGV, tenGV, gioiTinh, ngaySinh, tinhTrang, hocHam, hocVi ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
     * ;
     * try (Connection connection = dataSource.getConnection();
     * PreparedStatement stmt = connection.prepareStatement(sql)) {
     * stmt.setString(1, gv.getMaGV());
     * stmt.setString(2, gv.getHoGV());
     * stmt.setString(3, gv.getTenGV());
     * stmt.setString(4, gv.getGioiTinh());
     * stmt.setDate(5, new java.sql.Date(gv.getNgaySinh().getTime()));
     * stmt.setString(6, gv.getTinhTrang());
     * stmt.setString(7, gv.getHocHam());
     * stmt.setString(8, gv.getHocVi());
     * return stmt.executeUpdate() > 0;
     * } catch (SQLException e) {
     * e.printStackTrace();
     * return false;
     * }
     * }
     */
    public boolean addGiangVien(GiangVien gv) {
        // Kiểm tra mã giảng viên đã tồn tại chưa
        String checkSql = "SELECT COUNT(*) FROM GiangVien WHERE TRIM(maGV) = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, gv.getMaGV());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Mã giảng viên đã tồn tại
                return false;
            }

            String sql = "INSERT INTO GiangVien(maGV, hoGV, tenGV, gioiTinh, ngaySinh, tinhTrang, hocHam, hocVi) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, gv.getMaGV());
                stmt.setString(2, gv.getHoGV());
                stmt.setString(3, gv.getTenGV());
                stmt.setString(4, gv.getGioiTinh());
                stmt.setDate(5, new java.sql.Date(gv.getNgaySinh().getTime()));
                stmt.setString(6, gv.getTinhTrang());
                stmt.setString(7, gv.getHocHam());
                stmt.setString(8, gv.getHocVi());

                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteGiangVien(GiangVien gv) {
        String sql = "DELETE FROM GiangVien WHERE maGV = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, gv.getMaGV());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGiangVien(GiangVien gvUpdate) {
        String sql = "UPDATE GiangVien SET hoGV = ?, tenGV = ?, gioiTinh = ?, ngaySinh = ?, tinhTrang = ?, hocHam = ?, hocVi = ? WHERE maGV = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, gvUpdate.getHoGV());
            stmt.setString(2, gvUpdate.getTenGV());
            stmt.setString(3, gvUpdate.getGioiTinh());
            stmt.setDate(4, new java.sql.Date(gvUpdate.getNgaySinh().getTime()));
            stmt.setString(5, gvUpdate.getTinhTrang());
            stmt.setString(6, gvUpdate.getHocHam());
            stmt.setString(7, gvUpdate.getHocVi());
            stmt.setString(8, gvUpdate.getMaGV());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public GiangVien findGVById(String maGV) {
        String sql = "SELECT * FROM GiangVien WHERE TRIM(maGV) = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maGV);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                GiangVien gv = new GiangVien();
                gv.setMaGV(rs.getString("maGV"));
                gv.setHoGV(rs.getString("hoGV"));
                gv.setTenGV(rs.getString("tenGV"));
                gv.setGioiTinh(rs.getString("gioiTinh"));
                gv.setNgaySinh(rs.getDate("ngaySinh"));
                gv.setTinhTrang(rs.getString("tinhTrang"));
                gv.setHocHam(rs.getString("hocHam"));
                gv.setHocVi(rs.getString("hocVi"));
                return gv;
            } else {
                System.out.println("Không tìm thấy giảng viên với mã: " + maGV);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn findById:");
            e.printStackTrace();
        }
        return null;
    }

    public List<GiangVien> searchGiangVien(String maGV, String tenGV) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("Search_GiangVien")
                    .declareParameters(
                            new SqlParameter("p_maGV", OracleTypes.VARCHAR),
                            new SqlParameter("p_tenGV", OracleTypes.VARCHAR),
                            new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new GiangVienRowMapper()));

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_maGV", maGV == null ? "" : maGV);
            inParams.put("p_tenGV", tenGV == null ? "" : tenGV);
            Map<String, Object> result = jdbcCall.execute(inParams);
            return (List<GiangVien>) result.get("p_cursor");

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi procedure Search_GiangVien: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}
