package com.courseregist.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Entity
@Table(name = "GIANGVIEN")
public class GiangVien {
    @Id
    @Column(name = "MaGV")
    @NotEmpty(message = "Mã giảng viên không được để trống.")
    private String maGV;

    @Column(name = "HoGV")
    @NotEmpty(message = "Họ giảng viên không được để trống.")
    private String hoGV;

    @Column(name = "TenGV")
    @NotEmpty(message = "Tên giảng viên không được để trống.")
    private String tenGV;

    @Column(name = "GioiTinh")
    @NotEmpty(message = "Giới tính không được để trống.")
    private String gioiTinh;

    @Column(name = "NgaySinh")
    @NotNull(message = "Ngày sinh không được để trống.")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngaySinh;

    @Column(name = "TinhTrang")
    @NotEmpty(message = "Tình trạng không được để trống.")
    private String tinhTrang;

    @Column(name = "HocHam")
    @NotEmpty(message = "Học hàm không được để trống.")
    private String hocHam;

    @Column(name = "HocVi")
    @NotEmpty(message = "Học vị không được để trống.")
    private String hocVi;

    public GiangVien() {
    }

    public GiangVien(String maGV, String hoGV, String tenGV, String gioiTinh, Date ngaySinh, String tinhTrang,
            String hocHam, String hocVi) {
        this.maGV = maGV;
        this.hoGV = hoGV;
        this.tenGV = tenGV;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.tinhTrang = tinhTrang;
        this.hocHam = hocHam;
        this.hocVi = hocVi;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }

    public String getHoGV() {
        return hoGV;
    }

    public void setHoGV(String hoGV) {
        this.hoGV = hoGV;
    }

    public String getTenGV() {
        return tenGV;
    }

    public void setTenGV(String tenGV) {
        this.tenGV = tenGV;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public String getHocHam() {
        return hocHam;
    }

    public void setHocHam(String hocHam) {
        this.hocHam = hocHam;
    }

    public String getHocVi() {
        return hocVi;
    }

    public void setHocVi(String hocVi) {
        this.hocVi = hocVi;
    }
}
