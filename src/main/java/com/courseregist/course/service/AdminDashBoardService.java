package com.courseregist.course.service;

import com.courseregist.course.repository.AdminDashBoardRepository;
//import com.courseregist.course.repository.ChiTietDKRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
// Xử lý nghiệp vụ khi làm report
public class AdminDashBoardService {

    private final AdminDashBoardRepository dashboardRepository;

    @Autowired
    public AdminDashBoardService(AdminDashBoardRepository repository) {
        this.dashboardRepository = repository;
    }

    public Integer laySoLuongSinhVienDKHPHienTai() {
        Integer soLuong = dashboardRepository.soLuongSinhVienDKHP();
        return soLuong;
    }

    public Double trungBinhTinChi() {
        Double tinChi = dashboardRepository.trungBinhTinChi();
        return tinChi;
    }

    public Integer soHocPhan() {
        int tinChi = dashboardRepository.soLuongHocPhan();
        return tinChi;
    }

}
