package com.courseregist.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "DANGKYLICH")
public class DangKyLich {

    @Id
    @Column(name = "MaLichDay")
    private String maLichDay;

    @Id
    @Column(name = "MaPhieuDKDay")
    private String maPhieuDKDay;

}
