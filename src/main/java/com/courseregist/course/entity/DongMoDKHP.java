package com.courseregist.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table
public class DongMoDKHP {
    private int maDK;
    private String maHK;
    private boolean trangThai; // true: mở, false: đóng
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    public DongMoDKHP() {
    }

    public DongMoDKHP(int maDK, String maHK, boolean trangThai, LocalDateTime ngayBatDau,
            LocalDateTime ngayKetThuc) {
        this.maDK = maDK;
        this.maHK = maHK;
        this.trangThai = trangThai;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    // Getter & Setter
    public int getMaDK() {
        return maDK;
    }

    public void setMaDK(int maDK) {
        this.maDK = maDK;
    }

    public String getMaHK() {
        return maHK;
    }

    public void setMaHK(String maHK) {
        this.maHK = maHK;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

}
