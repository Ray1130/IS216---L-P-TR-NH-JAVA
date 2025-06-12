package com.courseregist.course.controller;

import com.courseregist.course.DTO.LichDangKyDTO; 
import com.courseregist.course.entity.LichDay; 
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
import java.util.LinkedHashMap; 
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

    @GetMapping("/dashboarddkgd")
    public String showDashboardDKGD() {
        return "dashboarddkgd";
    }

    @GetMapping("/dangkymonday")
    public String showMonHocPage(@RequestParam(name = "maMH", required = false) String maMH,
                                 @RequestParam(name = "tenMH", required = false) String tenMH,
                                 Model model) {
        List<MonHoc> danhSach;

        if ((maMH != null && !maMH.isEmpty()) || (tenMH != null && !tenMH.isEmpty())) {
            danhSach = dkgdService.searchMonHoc(maMH, tenMH); 
            model.addAttribute("isSearch", true);
        } else {
            danhSach = dkgdService.getDanhSachMH(); 
            model.addAttribute("isSearch", false);
        }

        model.addAttribute("monHocList", danhSach);
        return "dangkymonday";
    }

    @PostMapping("/luuphieudangky")
    public String dangKyMonDay(Authentication authentication,
                               @RequestParam("selectedMon") List<String> maMHList,
                               RedirectAttributes redirectAttributes) {
        String maGV = authentication.getName() != null ? authentication.getName().trim() : null;
        String maHK = dkgdService.getMaHKMoiNhat();

        System.out.println("CONTROLLER - MaGV: " + maGV);
        System.out.println("CONTROLLER - MaHK: " + maHK);
        System.out.println("CONTROLLER - Danh sách MaMH GV đăng ký: " + maMHList);

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
        redirectAttributes.addFlashAttribute("ketQuaDangKy", ketQua);

        return "redirect:/lecturer/dangkymonday";
    }


    @GetMapping("/dangkylichday")
    public String showDangKyLichDayPage() {
        return "dangkylichday";
    }

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


    @GetMapping("/danhsachdkgd")
    public String showDanhSachDKGD(Model model, Authentication authentication) {

        String username = authentication.getName();
        String maGVRaw = username; 
        String maGV = maGVRaw != null ? maGVRaw.trim() : null; 
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
        return "redirect:/lecturer/danhsachdkgd";
    }

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

    @GetMapping("/danhsachphancong")
    public String showDanhSachPhanCong(Authentication authentication,
                                       @RequestParam(name = "maLop", required = false) String maLop,
                                       @RequestParam(name = "tenMH", required = false) String tenMH,
                                       Model model) {
        String maGV = authentication.getName() != null ? authentication.getName().trim() : null;
        List<HPDTO> danhSach;
        if ((maLop != null && !maLop.isEmpty()) || (tenMH != null && !tenMH.isEmpty())) {
            danhSach = dkgdService.searchHPOfGV(maLop, tenMH, maGV);
            model.addAttribute("isSearch", true); 
        } else {
            danhSach = dkgdService.getDanhSachHocPhanOfGV(maGV); 
            model.addAttribute("isSearch", false);
        }
        System.out.println("Danh sách lớp học phần:");
        danhSach.forEach(System.out::println);
        model.addAttribute("lopHocList", danhSach);
        return "danhsachphancong";
    }

    @GetMapping("/danhsachphancong/dssvOfGV/{maLop}")
    public String getDanhSachSinhVien(@PathVariable String maLop, Model model) {
        HPDTO hocPhanDetails = adminService.findLopHocById(maLop);
        if (hocPhanDetails == null) {
            model.addAttribute("errorMessage", "Không tìm thấy lớp học với mã: " + maLop);
            return "error_page";
        }
        model.addAttribute("hocPhan", hocPhanDetails); 

        List<SinhVien> danhSachSinhVien;
        try {
            danhSachSinhVien = dkhpService.getSinhVienDangKy(maLop);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải danh sách sinh viên.");
            danhSachSinhVien = new ArrayList<>(); 
        }
        if (danhSachSinhVien.isEmpty()) {
            System.out.println("Không có sinh viên nào đăng ký cho MaLop: " + maLop);
            model.addAttribute("message", "Chưa có sinh viên nào đăng ký lớp học này.");
        }
        model.addAttribute("danhSachSinhVien", danhSachSinhVien);

        return "dssvOfGV";
    }

    private static final String DSSV_EXPORT_SPREADSHEET_ID = "1t12JnroOISkKDAuLb6VAPSvuYYjI_vbNst7f3GEKogM";

    private static final String DSSV_EXPORT_TARGET_SHEET_NAME = "ExportData";

    @GetMapping("/export/lophoc/{maLop}")
    public void exportSinhVienTheoLop(
            @PathVariable String maLop,
            HttpServletResponse response) {
        try {

            List<SinhVien> danhSachSV = dkhpService.getSinhVienDangKy(maLop);

            List<Object> headers = Arrays.asList("STT", "MSSV", "Họ và tên"); 
            List<List<Object>> dataForSheet = new ArrayList<>();
            int stt = 1;
            if (danhSachSV != null) {
                for (SinhVien sv : danhSachSV) {
                    dataForSheet.add(Arrays.asList(
                            stt++,
                            sv.getMaSV(),
                            sv.getHoTen()

                    ));
                }
            }

            googleSheetService.prepareDataOnExportSheet(
                    DSSV_EXPORT_SPREADSHEET_ID, 
                    DSSV_EXPORT_TARGET_SHEET_NAME, 
                    dataForSheet, 
                    headers
            );

            String downloadFileName = "DSSV_" + maLop.replace(".", "_").replace("/", "_");
            googleSheetService.downloadExportSheetAsExcel(
                    DSSV_EXPORT_SPREADSHEET_ID,
                    downloadFileName,
                    response);

        } catch (IOException e) {
            System.err.println("Lỗi I/O khi xuất file cho mã lớp " + maLop + ": " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) { 
            System.err.println("Lỗi không xác định khi xuất file cho mã lớp " + maLop + ": " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
}
