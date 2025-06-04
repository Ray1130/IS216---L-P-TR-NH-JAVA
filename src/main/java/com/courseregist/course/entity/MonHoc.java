package com.courseregist.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@Entity
@Table(name = "MONHOC")

public class MonHoc {
    @Id
    @Column(name = "MaMH")
    @NotEmpty(message = "Mã môn học không được để trống.")
    private String maMH;
    @Column(name = "TenMH")
    @NotEmpty(message = "Tên môn học không được để trống.")
    private String tenMH;
    @Column(name = "LoaiMon")
    @NotEmpty(message = "Loại môn học không được để trống.")
    private String loaiMon;
    @Column(name = "SoTC")
    @NotNull(message = "Số tín chỉ không được để trống.")
    @Min(value = 1, message = "Số tín chỉ phải lớn hơn 0.")
    private int soTinChi;
    @Column(name = "MaKhoa")
    @NotEmpty(message = "Mã khoa không được để trống.")
    private String maKhoa;
    @Column(name = "TenKhoa")
    private String tenKhoa;

    public MonHoc() {
    }

    public MonHoc(String maMH, String tenMH, String loaiMon, int soTinChi, String maKhoa) {
        this.maMH = maMH;
        this.tenMH = tenMH;
        this.loaiMon = loaiMon;
        this.soTinChi = soTinChi;
        this.maKhoa = maKhoa;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getMaMH() {
        return maMH;
    }

    public String getTenMH() {
        return tenMH;
    }

    public String getLoaiMon() {
        return loaiMon;
    }

    public String getMaKhoa() {
        return maKhoa;
    }

    public int getSoTinChi() {
        return soTinChi;
    }

    public String getTenKhoa() {
        return tenKhoa;
    }

    public void setMaMH(String maMH) {
        this.maMH = maMH;
    }

    public void setTenMH(String tenMH) {
        this.tenMH = tenMH;
    }

    public void setLoaiMon(String loaiMon) {
        this.loaiMon = loaiMon;
    }

    public void setSoTinChi(int soTinChi) {
        this.soTinChi = soTinChi;
    }

    public void setMaKhoa(String maKhoa) {
        this.maKhoa = maKhoa;
    }

    public void setTenKhoa(String tenKhoa) {
        this.tenKhoa = tenKhoa;
    }
}
