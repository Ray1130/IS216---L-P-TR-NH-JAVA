package com.courseregist.course.repository;

import com.courseregist.course.DTO.HocKyRowMapper;
import com.courseregist.course.entity.HocKy;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HocKyRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public HocKyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // lấy tất cả học kỳ
    public List<HocKy> getAllHocKy() {
        String sql = "SELECT * FROM HOCKY";
        return jdbcTemplate.query(sql, new HocKyRowMapper());
    }
    // lấy năm học

    public List<String> getDistinctNamHoc() {
        String sql = "SELECT DISTINCT NamHoc FROM HOCKY ORDER BY NamHoc DESC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // for chart
    public List<String> findDistinctHocKy() {
        String sql = "SELECT DISTINCT TENHK || ' ' || NAMHOC AS NH FROM HOCKY ORDER BY NH DESC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

}
