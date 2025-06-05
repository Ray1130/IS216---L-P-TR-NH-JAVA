package com.courseregist.course.service;

import com.courseregist.course.DTO.HPDTO;
import com.courseregist.course.entity.GiangVien;
import com.courseregist.course.entity.MonHoc;
import com.courseregist.course.entity.SinhVien;
import com.courseregist.course.repository.GiangVienRepository;
import com.courseregist.course.repository.MonHocRepository;
import com.courseregist.course.repository.SinhVienRepository;

import com.courseregist.course.repository.HPRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class AdminService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MonHocRepository mhRepository;
    @Autowired
    private HPRepository hpRepository;

    public List<MonHoc> getDanhSachMH() {
        return mhRepository.getDanhSachMH();
    }

    // thêm môn học
    public boolean addMonHoc(MonHoc monHoc) {
        if (monHoc.getMaMH() == null || monHoc.getMaMH().isEmpty()) {
            return false;
        }
        return mhRepository.addMonHoc(monHoc);
    }

    // thay đổi parameter truyền vào thành entity MonHoc
    public boolean deleteMonHoc(MonHoc monHoc) {
        if (monHoc.getMaMH() == null || monHoc.getMaMH().isEmpty()) {
            return false;
        }
        return mhRepository.deleteMonHoc(monHoc);
    }

    // Cập nhật thông tin môn học
    public boolean updateMonHoc(MonHoc monHoc) {
        return mhRepository.updateMonHoc(monHoc);
    }

    // tìm môn học theo mã
    public MonHoc findById(String maMH) {
        return mhRepository.findById(maMH);
    }

    // Lấy danh sách tên môn học duy nhất
    public List<String> findDistinctTenMonHoc() {
        return mhRepository.findDistinctTenMonHoc();
    }

    // Tìm môn học theo mã hoặc tên
    public List<MonHoc> searchMonHoc(String maMH, String tenMH) {
        String maMHTrimmed = (maMH != null) ? maMH.trim() : "";
        String tenMHTrimmed = (tenMH != null) ? tenMH.trim() : "";

        // nếu không nhập gì thì vẫn ra dữ liệu bình thường
        if (maMHTrimmed.isEmpty() && tenMHTrimmed.isEmpty()) {

            return mhRepository.getDanhSachMH();
        }

        return mhRepository.searchMonHoc(maMHTrimmed, tenMHTrimmed);
    }

    @Autowired
    private SinhVienRepository svRepository;

    public List<SinhVien> getDanhSachSV() {
        return svRepository.getDanhSachSV();
    }

    // Thêm sinh viên, thay đổi parameter truyền vào thành entity SinhVien
    public boolean addSinhVien(SinhVien sinhVien) {
        if (sinhVien.getMaSV() == null || sinhVien.getMaSV().isEmpty()) {
            return false;
        }
        return svRepository.addSinhVien(sinhVien);
    }

    // Xóa sinh viên
    public boolean deleteSinhVien(SinhVien sinhVien) {
        if (sinhVien.getMaSV() == null || sinhVien.getMaSV().isEmpty()) {
            return false;
        }
        return svRepository.deleteSinhVien(sinhVien);
    }

    // Cập nhật thông tin sinh viên
    public boolean updateSinhVien(SinhVien sinhVien) {
        return svRepository.updateSinhVien(sinhVien);
    }

    // Tìm sinh viên theo mã
    public SinhVien findByIdSV(String maSV) {
        return svRepository.findByIdSV(maSV);
    }

    public List<SinhVien> searchSinhVien(String maSV, String hoTenSV) {

        String maSVTrimmed = (maSV != null) ? maSV.trim() : "";
        String hoTenSVTrimmed = (hoTenSV != null) ? hoTenSV.trim() : "";

        // nếu không nhập gì thì vẫn ra dữ liệu bình thường
        if (maSVTrimmed.isEmpty() && hoTenSVTrimmed.isEmpty()) {
            return svRepository.getDanhSachSV();
        }

        return svRepository.searchSinhVien(maSVTrimmed, hoTenSVTrimmed);
    }

    /** QUẢN LÝ GIẢNG VIÊN **/

    @Autowired
    private GiangVienRepository gvRepository;

    public List<GiangVien> getDanhSachGiangVien() {
        return gvRepository.getDanhSachGiangVien();
    }

    public boolean addGiangVien(GiangVien giangVien) {
        if (giangVien.getMaGV() == null || giangVien.getMaGV().isEmpty()) {
            return false;
        }
        return gvRepository.addGiangVien(giangVien);
    }

    public boolean deleteGiangVien(GiangVien giangVien) {
        if (giangVien.getMaGV() == null || giangVien.getMaGV().isEmpty()) {
            return false;
        }
        return gvRepository.deleteGiangVien(giangVien);
    }

    public boolean updateGiangVien(GiangVien giangVien) {
        return gvRepository.updateGiangVien(giangVien);
    }

    public GiangVien findGVById(String maGV) {
        return gvRepository.findGVById(maGV);
    }

    // Tìm giảng viên theo mã hoặc họ tên (gộp họ + tên thành 1 trường tìm kiếm)
    public List<GiangVien> searchGiangVien(String maGV, String hoTenGV) {
        String maGVTrimmed = (maGV != null) ? maGV.trim() : "";
        String hoTenGVTrimmed = (hoTenGV != null) ? hoTenGV.trim() : "";

        if (maGVTrimmed.isEmpty() && hoTenGVTrimmed.isEmpty()) {
            return gvRepository.getDanhSachGiangVien();
        }
        return gvRepository.searchGiangVien(maGVTrimmed, hoTenGVTrimmed);
    }
}
