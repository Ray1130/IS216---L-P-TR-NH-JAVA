package com.courseregist.course.DTO;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "LOPHOC")
public class HPDTO {
    @Id
    private String maLop;
    private int siSo;
    private int thuNgayHoc;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private int cachTuan;
    private String tietHoc;
    private String ngonNguGiangDay;
    private String maMH;
    private String maHK;
    private String phongHoc;
    private String maGV;
    private String hoGV;
    private String tenGV;
    private String tenMH;
    private int soTinChi;

    public HPDTO() {
    }

    public HPDTO(String maLop, int siSo, int thuNgayHoc, Date ngayBatDau, Date ngayKetThuc,
            int cachTuan, String tietHoc, String ngonNguGiangDay,
            String maMH, String tenMH, String hoGV, String tenGV, int soTinChi, String maGV) {
        this.maLop = maLop;
        this.siSo = siSo;
        this.thuNgayHoc = thuNgayHoc;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.cachTuan = cachTuan;
        this.tietHoc = tietHoc;
        this.ngonNguGiangDay = ngonNguGiangDay;
        this.maMH = maMH;
        this.tenMH = tenMH;
        this.hoGV = hoGV;
        this.tenGV = tenGV;
        this.soTinChi = soTinChi;
        this.maGV = maGV;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }

    public void setSiSo(int siSo) {
        this.siSo = siSo;
    }

    public void setThuNgayHoc(int thuNgayHoc) {
        this.thuNgayHoc = thuNgayHoc;
    }

    public void setTietHoc(String tietHoc) {
        this.tietHoc = tietHoc;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public void setCachTuan(int cachTuan) {
        this.cachTuan = cachTuan;
    }

    public void setNgonNguGiangDay(String ngonNguGiangDay) {
        this.ngonNguGiangDay = ngonNguGiangDay;
    }

    public void setMaMH(String maMH) {
        this.maMH = maMH;
    }

    public void setTenMH(String tenMH) {
        this.tenMH = tenMH;
    }

    public void setHoGV(String hoGV) {
        this.hoGV = hoGV;
    }

    public void setTenGV(String tenGV) {
        this.tenGV = tenGV;
    }

    public void setSoTinChi(int soTinChi) {
        this.soTinChi = soTinChi;
    }

    public void setMaHK(String maHK) {
        this.maHK = maHK;
    }

    public void setPhongHoc(String phongHoc) {
        this.phongHoc = phongHoc;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }
}