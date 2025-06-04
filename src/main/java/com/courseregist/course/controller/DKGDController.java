package com.courseregist.course.controller;

import com.courseregist.course.DTO.LichDangKyDTO; // NEW for /danhsachdkgd
import com.courseregist.course.entity.LichDay; // for danh sách lịch dạy hiển thị
import com.courseregist.course.entity.MonHoc;
import com.courseregist.course.service.DKGDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.courseregist.course.service.AdminService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.awt.font.ImageGraphicAttribute;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap; // for GiangVien DANGKY_MONDAY
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/lecturer")
public class DKGDController {

    @Autowired
    private final DKGDService dkgdService;

    public DKGDController(DKGDService dkgdService) {
        this.dkgdService = dkgdService;
    }

    // Điều hướng đến dashboard đăng ký giảng dạy (/lecturer/dashboardkgd)
    @GetMapping("/dashboarddkgd")
    public String showDashboardDKGD() {
        return "dashboarddkgd";
    }

    // Điều hướng đến trang đăng ký môn dạy (/lecturer/dangkymonday)
    // Trong mỗi Controller không được có GetMapping trùng nhau
    // Lấy danh sách môn học từ database môn học (nhờ vào DKGDService)
    @GetMapping("/dangkymonday")
    public String showMonHocPage(@RequestParam(name = "maMH", required = false) String maMH,
                                 @RequestParam(name = "tenMH", required = false) String tenMH,
                                 Model model) {
        List<MonHoc> danhSach;

        if ((maMH != null && !maMH.isEmpty()) || (tenMH != null && !tenMH.isEmpty())) {
            danhSach = dkgdService.searchMonHoc(maMH, tenMH); // Gọi hàm tìm kiếm
            model.addAttribute("isSearch", true); // Để biết đang ở trạng thái tìm kiếm
        } else {
            danhSach = dkgdService.getDanhSachMH(); // Lấy toàn bộ danh sách
            model.addAttribute("isSearch", false);
        }

        model.addAttribute("monHocList", danhSach);
        return "dangkymonday";
    }

    // Điều hướng giảng viên đăng ký thành công sau khi nhấn nút Xác nhận
    @PostMapping("/luuphieudangky")
    public String dangKyMonDay(Authentication authentication,
                               //@RequestParam String maHK,
                               @RequestParam("selectedMon") List<String> maMHList,
                               // RedirectAttributes dung de dieu huong + hien thong bao
                               RedirectAttributes redirectAttributes) {
        String maGV = authentication.getName() != null ? authentication.getName().trim() : null;
        String maHK = dkgdService.getMaHKMoiNhat();

        System.out.println("CONTROLLER - MaGV: " + maGV);
        System.out.println("CONTROLLER - MaHK: " + maHK);
        System.out.println("CONTROLLER - Danh sách MaMH GV đăng ký: " + maMHList); // do để mã MH CHAR(10) nên trống khi in ra ở Terminal

        Map<String, String> ketQua = new LinkedHashMap<>();

        for (String maMH : maMHList) {
            int result = dkgdService.dangKyMonDay(maGV, maHK, maMH);

            System.out.println("SERVICE - Result Code từ dangKyMonDay: " + result);

            switch (result) {
                case 1:
                    ketQua.put(maMH, "Đăng ký thành công");
                    break;
                case -1:
                    ketQua.put(maMH, "Đã đăng ký môn này trong học kỳ");
                    break;
                case -99:
                    ketQua.put(maMH, "Lỗi hệ thống khi đăng ký");
                    break;
                default:
                    ketQua.put(maMH, "Lỗi không xác định");
            }
        }
        // Hiển thị thông báo đăng ký thành công
        redirectAttributes.addFlashAttribute("ketQuaDangKy", ketQua);

        return "redirect:/lecturer/dangkymonday";
    }


    // Điều hướng đến trang đăng ký lịch dạy (/lecturer/dangkylichday)
    @GetMapping("/dangkylichday")
    public String showDangKyLichDayPage() {
        return "dangkylichday";
    }

    // Lưu thông tin đăng ký thời gian dạy
    @PostMapping("/luulichday")
    public String dangKyLichDay(Authentication authentication,
                                @RequestParam List<String> danhSachLichDay,
                                RedirectAttributes redirectAttributes) {
        String maGV = authentication.getName() != null ? authentication.getName().trim() : null;
        String maHK = dkgdService.getMaHKMoiNhat();

        System.out.println("CONTROLLER - MaGV: " + maGV);
        System.out.println("CONTROLLER - MaHK: " + maHK);
        System.out.println("CONTROLLER - Danh sách lịch dạy GV đã đăng ký: " + danhSachLichDay);

        boolean allSuccess = true;

        for(String lichDay : danhSachLichDay) {
            int result = dkgdService.dangKyLichDay(maGV, maHK, lichDay);
            System.out.println("SERVICE - Result Code từ dangKyLichDay: " + result);
            if(result != 1) {
                allSuccess = false;
            }
        }

        if(allSuccess) {
            redirectAttributes.addFlashAttribute("message", "Thêm lịch dạy thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Thêm lịch dạy thất bại với một hoặc nhiều tiết!");
        }

        return "redirect:/lecturer/dangkylichday";
    }


    // Điều hướng đến trang danh sách đăng ký giảng dạy (/lecturer/danhsachdkgd)
    @GetMapping("/danhsachdkgd")
    public String showDanhSachDKGD(Model model, Authentication authentication) {

        String username = authentication.getName();
        String maGVRaw = username; // Giữ lại giá trị gốc để log nếu cần
        String maGV = maGVRaw != null ? maGVRaw.trim() : null; // Trim khoảng trắng
        System.out.println("CONTROLLER - MaGV sẽ truyền vào service: " + maGV);

        List<MonHoc> danhSachMonHoc = dkgdService.getDanhSachMHDaDangKy(maGV);
        List<LichDangKyDTO> danhSachLichDay = dkgdService.getDanhSachLDDaDangKy(maGV);

        if (danhSachMonHoc == null || danhSachMonHoc.isEmpty()) {
            System.out.println("Không có môn học nào được đăng ký cho MaGV: " + maGV);
        }

        if (danhSachLichDay == null || danhSachLichDay.isEmpty()) {
            System.out.println("Không có lịch dạy nào được đăng ký cho MaGV: " + maGV);
        }

        model.addAttribute("monHocList", danhSachMonHoc);
        model.addAttribute("lichDayList", danhSachLichDay);

        return "danhsachdkgd";
    }

    // Xoa mon hoc da dang ky cua giang vien
    @PostMapping("/delete/{maMH}")
    public String xoaMonHoc(@PathVariable("maMH") String maMH,
                            RedirectAttributes redirectAttributes) {
        System.out.println("Đã nhận từ form: " + maMH);

        try {
            dkgdService.deleteMonHoc_GV(maMH);
            redirectAttributes.addFlashAttribute("message", "Xóa môn học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa môn học thất bại!");
        }
        return "redirect:/lecturer/danhsachdkgd"; // chuyển về trang danh sách dang ky giang day
    }

    // Xoa lich day da dang ky cua giang vien
    @PostMapping("/xoaTiet/{thu}")
    public String xoaTiet(@PathVariable("thu") String thu,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        String maGV = authentication.getName();
        System.out.println("Xóa thứ: " + thu + ", GV: " + maGV);

        try {
            boolean success = dkgdService.deleteTietDay(maGV, thu);
            if (success) {
                redirectAttributes.addFlashAttribute("message", "Xóa tất cả tiết trong thứ thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy hoặc xóa thất bại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa thứ!");
        }

        return "redirect:/lecturer/danhsachdkgd";
    }


    // Điều hướng đến trang danh sách lớp dạy được phân công (/lecturer/danhsachphancong)
    // Trang này để phòng đào tạo tự sắp xếp thủ công sau đó cập nhật lên hệ thống chứ không thao tác trên trang này
    @GetMapping("/danhsachphancong")
    public String showDanhSachPhanCong() { return "danhsachphancong"; }

}
