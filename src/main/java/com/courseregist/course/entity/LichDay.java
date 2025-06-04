package com.courseregist.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "LICHDAY")
public class LichDay {
    @Id
    @Column(name = "MaLichDay")
    private String maLichDay;
    private String maLop;
    private Date Ngay;
    private String Tiet;
    private int thu;
    private String buoi;
    private Date thoiGian;

}
