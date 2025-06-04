package com.courseregist.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Date;

@Data
@Entity

public class HocKy {
    @Id
    private String maHK;
    private String tenHK;
    private String namHoc;
    private Date ngayBatDau;
    private Date ngayKetThuc;

    public HocKy() {
    }

    public HocKy(String maHK, String tenHK, String namHoc, Date ngayBatDau, Date ngayKetThuc) {
        this.maHK = maHK;
        this.tenHK = tenHK;
        this.namHoc = namHoc;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

}