package com.courseregist.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Table(name = "SINHVIEN")

public class SinhVien {
    @Id
    @Column(name = "MaSV")
    @NotEmpty(message = "Mã sinh viên không được để trống.")
    private String maSV;

    @Column(name = "HoSV")
    @NotEmpty(message = "Họ sinh viên không được để trống.")
    private String hoSV;

    @Column(name = "TenSV")
    @NotEmpty(message = "Tên sinh viên không được để trống.")
    private String tenSV;

    @Column(name = "GioiTinh")
    @NotEmpty(message = "Giới tính không được để trống.")
    private String gioiTinh;

    @Column(name = "NgaySinh")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày sinh không được để trống.")
    private Date ngaySinh;

    @Column(name = "TinhTrang")
    private String tinhTrang;

    @Column(name = "MaHe")
    @NotEmpty(message = "Mã hệ không được để trống.")
    private String maHe;

    @Column(name = "MaNganh")
    @NotEmpty(message = "Mã ngành không được để trống.")
    private String maNganh;

    @Transient
    private String hoTen;

    public SinhVien() {
    }

    public SinhVien(String maSV, String hoSV, String tenSV, String gioiTinh,
            Date ngaySinh, String tinhTrang, String maHe, String maNganh) {
        this.maSV = maSV;
        this.hoSV = hoSV;
        this.tenSV = tenSV;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.tinhTrang = tinhTrang;
        this.maHe = maHe;
        this.maNganh = maNganh;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getMaSV() {
        return maSV;
    }

    public String getHoSV() {
        return hoSV;
    }

    public String getTenSV() {
        return tenSV;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public String getMaHe() {
        return maHe;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getHoTen() {
        return hoTen != null ? hoTen : (hoSV + " " + tenSV);
    }

    public void setHoSV(String hoSV) {
        this.hoSV = hoSV;
    }

    public void setTenSV(String tenSV) {
        this.tenSV = tenSV;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public void setMaHe(String maHe) {
        this.maHe = maHe;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }
}