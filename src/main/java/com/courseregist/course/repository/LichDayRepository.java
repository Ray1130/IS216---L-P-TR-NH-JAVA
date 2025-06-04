package com.courseregist.course.repository;

import com.courseregist.course.DTO.LichDangKyDTO;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class LichDayRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<LichDangKyDTO> getDanhSachLDDaDangKy(String maGV) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GET_TIET_SANG_CHIEU")
                .declareParameters(
                        new SqlParameter("p_magv", OracleTypes.VARCHAR),
                        new SqlOutParameter("p_cursor", OracleTypes.CURSOR, new RowMapper<LichDangKyDTO>() {
                            @Override
                            public LichDangKyDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                                LichDangKyDTO dto = new LichDangKyDTO();
                                dto.setThu("Thứ " + rs.getInt("NGAYTRONGTUAN"));
                                int tiet = rs.getInt("TIET");
                                String buoi = rs.getString("BUOI");

                                if ("Sáng".equalsIgnoreCase(buoi)) {
                                    dto.setTietSang(new ArrayList<>(List.of(String.valueOf(tiet))));
                                    dto.setTietChieu(new ArrayList<>());
                                } else if ("Chiều".equalsIgnoreCase(buoi)) {
                                    dto.setTietChieu(new ArrayList<>(List.of(String.valueOf(tiet))));
                                    dto.setTietSang(new ArrayList<>());
                                }

                                return dto;
                            }
                        })
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_magv", maGV == null ? "" : maGV);
        Map<String, Object> result = jdbcCall.execute(inParams);

        List<LichDangKyDTO> rawList = (List<LichDangKyDTO>) result.get("p_cursor");

        Map<String, LichDangKyDTO> merged = new LinkedHashMap<>();

        for (LichDangKyDTO dto : rawList) {
            String thu = dto.getThu();
            merged.putIfAbsent(thu, new LichDangKyDTO(thu));

            LichDangKyDTO existing = merged.get(thu);
            existing.getTietSang().addAll(dto.getTietSang());
            existing.getTietChieu().addAll(dto.getTietChieu());
        }

        for (LichDangKyDTO dto : merged.values()) {
            dto.getTietSang().sort(Comparator.comparingInt(Integer::parseInt));
            dto.getTietChieu().sort(Comparator.comparingInt(Integer::parseInt));
        }

        return new ArrayList<>(merged.values());
    }
}
