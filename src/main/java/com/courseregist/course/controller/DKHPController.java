package com.courseregist.course.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.courseregist.course.DTO.HPDTO;
import com.courseregist.course.service.ChiTietDKService;
import com.courseregist.course.service.DKHPService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/DKHP")
public class DKHPController {
    @GetMapping("/dashboarddkhp")
    public String showDashboard() {
        return "dashboarddkhp";
    }

    @Autowired
    private final DKHPService dkhpService;
    private final ChiTietDKService ctDKService;

    public DKHPController(DKHPService dkhpService, ChiTietDKService chiTietDKService) {
        this.dkhpService = dkhpService;
        this.ctDKService = chiTietDKService;
    }

    @GetMapping("/formdkhp")
    public String showDangKyPage(@RequestParam(name = "maLop", required = false) String maLop,
            @RequestParam(name = "tenMH", required = false) String tenMH, Model model,
            RedirectAttributes redirectAttributes) {
        boolean isRegistrationOpen = dkhpService.isAnyRegistrationPeriodCurrentlyOpen();// có học kì nào mở không
        if (!isRegistrationOpen) {
            // Nếu KHÔNG trong thời gian đăng ký:
            // Chuyển hướng sinh viên đến một trang thông báo và không hiển thị form DKHP
            redirectAttributes.addFlashAttribute("errorMessageDkhp",
                    "Hiện không trong thời gian đăng ký học phần. Vui lòng kiểm tra lại lịch đăng ký.");
            return "redirect:/DKHP/dashboarddkhp";
        }
        List<HPDTO> danhSach;// lấy danh sách các học phần
        if ((maLop != null && !maLop.isEmpty()) || (tenMH != null && !tenMH.isEmpty())) {
            danhSach = dkhpService.searchHP(maLop, tenMH); // Gọi hàm tìm kiếm theo mã lớp, môn
            model.addAttribute("isSearch", true); // Để biết đang ở trạng thái tìm kiếm
        } else {
            danhSach = dkhpService.getDanhSachHP(); // Lấy toàn bộ danh sách
            model.addAttribute("isSearch", false);
        }
        model.addAttribute("lopHocList", danhSach);
        return "formdkhp";
    }

    // gửi 1 post để đăng ký - cái này ảnh hưởng ở giao diện
    @PostMapping("/dangky")
    public ResponseEntity<?> dangKyNhieuLop(
            Authentication authentication, // đăng kí nhận diện theo mã sinh viên đồng thời cũng là tên đăng nhập
            @RequestParam String maHK,
            @RequestParam List<String> maLop) {

        String username = authentication.getName();
        String maSVRaw = username;
        String maSV = maSVRaw != null ? maSVRaw.trim() : null; // Trim khoảng trắng
        List<String> ketQua = new ArrayList<>();
        for (String lop : maLop) {
            int result = dkhpService.dangKyHocPhan(maSV, maHK, lop); // result kiểm tra đăng kí thành công không, nếu
                                                                     // lỗi thì lỗi gì
            ketQua.add(lop + ": " + dkhpService.convertDangKyResult(result));
        }
        return ResponseEntity.ok(ketQua);// trả về http response với trạng thái 200OK
    }

    @GetMapping("/danhsachdkhp")
    public String showListDKHP(Model model, Authentication authentication) {
        String username = authentication.getName();
        String maSVRaw = username;
        String maSV = maSVRaw != null ? maSVRaw.trim() : null; // Trim khoảng trắng
        List<HPDTO> danhSachLop = dkhpService.getDanhSachHPDaDangKy(maSV);// lấy danh sách học phần theo mã sinh viên
        if (danhSachLop == null || danhSachLop.isEmpty()) {
            System.out.println("Không có lớp học nào được đăng ký cho MaSV: " + maSV);
        }
        model.addAttribute("hocPhanList", danhSachLop); // đặt tên để sau lấy dữ liệu trong view
        return "/danhsachdkhp";
    }

    @PostMapping("/delete/{maLop}")
    public String xoaHP(Authentication authentication,
            @PathVariable("maLop") String maLop, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            String maSVRaw = username;
            String maSV = maSVRaw != null ? maSVRaw.trim() : null;
            dkhpService.deleteHP(maSV, maLop);
            redirectAttributes.addFlashAttribute("message", "Xóa Lớp học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa lớp học thất bại!");
        }
        return "redirect:/DKHP/danhsachdkhp";
    }

    // lấy sĩ số của lớp
    @GetMapping("/{maLop}/siso")
    public ResponseEntity<Integer> getSiSoLop(@PathVariable String maLop) throws SQLException {
        int siSo = ctDKService.getSoSinhVienDangKy(maLop);
        return ResponseEntity.ok(siSo);
    }

}
