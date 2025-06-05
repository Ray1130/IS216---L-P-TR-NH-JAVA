package com.courseregist.course.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.courseregist.course.DTO.LichDangKyDTO;
import com.courseregist.course.entity.MonHoc;
import com.courseregist.course.entity.DongMoDKHP;
import com.courseregist.course.repository.MonHocRepository;
import com.courseregist.course.service.*;

import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;

import com.courseregist.course.DTO.HPDTO;
import com.courseregist.course.entity.DongMoDKHP;
import com.courseregist.course.entity.GiangVien;
import com.courseregist.course.repository.GiangVienRepository;
import com.courseregist.course.entity.SinhVien;
import com.courseregist.course.repository.SinhVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;

//import com.courseregist.course.DTO.HPDTO;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MonHocRepository mhRepo;
    private final AdminService adminService;
    private final HocKyService hockyService;
    private final DKHPService dkhpService;
    private AdminDashBoardService dashboardService;
    private GoogleSheetsService googleSheetService;
    private DKGDService dkgdService;

    @Autowired
    public AdminController(AdminService adminService, DKHPService dkhpService, HocKyService hockyService,
            GoogleSheetsService googleSheetService, AdminDashBoardService dashboardService, DKGDService dkgdService) {
        this.adminService = adminService;
        this.dkhpService = dkhpService;
        this.hockyService = hockyService;
        this.googleSheetService = googleSheetService;
        this.dashboardService = dashboardService;
        this.dkgdService = dkgdService;
    }

    private static final String DAILY_STATS_SHEET_NAME = "Trang tính3";
    private static final String DAILY_STATS_RANGE_FOR_CHART = DAILY_STATS_SHEET_NAME + "!A1:B";

    private static final String HOURLY_STATS_SHEET_NAME = "Trang tính2";
    private static final String HOURLY_STATS_RANGE_FOR_CHART = HOURLY_STATS_SHEET_NAME + "!A1:B";

    private static final String STUDENTS_BY_MAJOR_SHEET_NAME = "Trang tính1";
    private static final String STUDENTS_BY_MAJOR_RANGE_FOR_CHART = STUDENTS_BY_MAJOR_SHEET_NAME + "!A1:B";
    // sheet để export danh sách sinh viên
    private static final String DSSV_EXPORT_SPREADSHEET_ID = "1td9Zvk2gpWcH8GxMWjfkOuYPKT-VwC7ZSv_m9MKQ6xA";
    // trang trong cái sheet đó
    private static final String DSSV_EXPORT_TARGET_SHEET_NAME = "ExportData";

    // Bên trong class AdminController

    /**
     *
     * @param sheetData Dữ liệu đọc từ Google Sheet (có dạng List các dòng, mỗi dòng
     *                  là List các ô).
     * @param labelType Mô tả loại label (ví dụ: "ngày", "giờ", "ngành") dùng cho
     *                  thông báo lỗi.
     * @param valueType Mô tả loại giá trị (ví dụ: "số lượt", "số sinh viên") dùng
     *                  cho thông báo lỗi.
     * @return ResponseEntity chứa dữ liệu biểu đồ (labels và data) hoặc lỗi.
     */
    private Map<String, List<?>> parseSheetDataToChartData( // chuyển dữ liệu sheet thành dữ liệu
            List<List<Object>> sheetData,
            String labelHeaderName, // Tên cột cho label
            String valueHeaderName, // Tên cột cho value
            boolean valueIsInteger // true nếu giá trị là Integer, false nếu là Double
    ) throws IllegalArgumentException {
        // ... (Logic tìm cột theo headerName, bỏ qua dòng tiêu đề, parse dữ liệu)
        // ... (Ném IllegalArgumentException nếu có lỗi)
        if (sheetData == null || sheetData.isEmpty() || sheetData.get(0) == null) {
        }
        List<Object> headerRow = sheetData.get(0);
        int labelColIdx = -1, valueColIdx = -1;
        // Tìm index cột
        for (int i = 0; i < headerRow.size(); i++) {
            if (headerRow.get(i).toString().equalsIgnoreCase(labelHeaderName))
                labelColIdx = i;
            if (headerRow.get(i).toString().equalsIgnoreCase(valueHeaderName))
                valueColIdx = i;
        }
        if (labelColIdx == -1) // tìm cột label và value,so sánh string với tên đã đặt
            throw new IllegalArgumentException("Không tìm thấy cột label: " + labelHeaderName);
        if (valueColIdx == -1)
            throw new IllegalArgumentException("Không tìm thấy cột value: " + valueHeaderName);

        List<String> labels = new ArrayList<>();
        List<Number> dataPoints = new ArrayList<>();

        for (int i = 1; i < sheetData.size(); i++) {
            List<Object> row = sheetData.get(i);
            if (row != null && row.size() > Math.max(labelColIdx, valueColIdx) &&
                    row.get(labelColIdx) != null && row.get(valueColIdx) != null) {
                labels.add(row.get(labelColIdx).toString());
                try {
                    Number val;
                    if (valueIsInteger) {
                        val = Integer.parseInt(row.get(valueColIdx).toString().trim());
                    } else {
                        val = Double.parseDouble(row.get(valueColIdx).toString().trim());
                    }
                    dataPoints.add(val);
                } catch (NumberFormatException e) {
                    System.err.println("Lỗi parse số: " + row.get(valueColIdx) + " cho label: " + row.get(labelColIdx));
                    if (valueIsInteger)
                        dataPoints.add(0);
                    else
                        dataPoints.add(0.0);
                }
            }
        }
        Map<String, List<?>> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", dataPoints);
        return chartData;
    }
    // phân sinh viên theo ngành

    @GetMapping("/chart-data/phansinhvientheonganh")
    @ResponseBody
    public ResponseEntity<?> phanSinhVienTheoNganhForChart(
            @RequestParam(required = false, defaultValue = "") String monHoc,
            @RequestParam(required = false, defaultValue = "") String hocKy,
            Model model) {
        try {
            System.out.println("Chart phansinhvientheonganh: " + monHoc);
            System.out.println("Trong hoc ky: " + hocKy);

            String spreadsheetId = googleSheetService.SPREADSHEET_ID;
            List<List<Object>> sheetData = googleSheetService.readSheetData(spreadsheetId,
                    STUDENTS_BY_MAJOR_RANGE_FOR_CHART);

            List<Map<String, Object>> rawData = dashboardService.laySinhVienTheoNganhFilter(monHoc, hocKy); // Minh Long
                                                                                                            // bổ sung
                                                                                                            // phần lọc

            List<String> labels = new ArrayList<>();
            List<Integer> values = new ArrayList<>();

            for (Map<String, Object> row : rawData) {
                labels.add(row.get("MaNganh").toString());
                values.add(Integer.parseInt(row.get("so_luong_sinh_vien").toString()));
            }

            Map<String, List<?>> chartData = new HashMap<>();
            chartData.put("labels", labels);
            chartData.put("data", values);

            return ResponseEntity.ok(chartData);
        } catch (IllegalArgumentException e) {
            System.err.println("Lỗi parse dữ liệu sinh viên theo ngành: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy dữ liệu biểu đồ số sinh viên theo ngành từ sheet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể tải dữ liệu biểu đồ số sinh viên theo ngành.");
        }
    }

    @ResponseBody
    @GetMapping("/chart-data/luotdangkyngay")
    public ResponseEntity<?> getDailyRegistrationStatsForChart( // lấy lượt đăng ký theo ngành
            @RequestParam(required = false, defaultValue = "") String monHoc, // lọc trên môn học, học kì
            @RequestParam(required = false, defaultValue = "") String hocKy,
            Model model) {
        try {
            String spreadsheetId = googleSheetService.SPREADSHEET_ID;
            List<List<Object>> sheetData = googleSheetService.readSheetData(spreadsheetId, DAILY_STATS_RANGE_FOR_CHART);
            System.out.println("DEBUG: Using range: " + DAILY_STATS_RANGE_FOR_CHART); // In ra range đang dùng
            if (sheetData == null || sheetData.isEmpty()) {
                System.out.println("DEBUG: sheetData is NULL or EMPTY!");
            } else {
                List<Object> headerRowActual = sheetData.get(0);
                System.out.println("DEBUG: Actual header row read by Java: " + headerRowActual);
                if (headerRowActual != null) {
                    for (int i = 0; i < headerRowActual.size(); i++) {
                        Object cellValue = headerRowActual.get(i);
                        if (cellValue != null) {
                            System.out.println("DEBUG: Header Cell [" + i + "]: '" + cellValue.toString()
                                    + "' (length: " + cellValue.toString().length() + ")");
                            System.out
                                    .println("DEBUG: Header Cell [" + i + "] (trimmed): '" + cellValue.toString().trim()
                                            + "' (length: " + cellValue.toString().trim().length() + ")");
                        } else {
                            System.out.println("DEBUG: Header Cell [" + i + "]: null");
                        }
                    }
                }
                System.out.println("DEBUG: Expected labelHeaderName (trimmed): '" + "Ngày đăng ký".trim()
                        + "' (length: " + "Ngày đăng ký".trim().length() + ")");
            }
            // ================== DEBUG CODE END ====================

            // NEW ADDED BY MINH LONG NGUYEN
            // Gọi service xử lý SQL, đã lọc sẵn theo monHoc & hocKy nếu có
            List<Map<String, Object>> rawData = dashboardService.theoDoiSoLuotDKTheoNgayFilter(monHoc, hocKy);

            // Duyệt dữ liệu để lấy nhãn (ngày) và giá trị (lượt đăng ký)
            List<String> labels = new ArrayList<>();
            List<Integer> values = new ArrayList<>();

            for (Map<String, Object> row : rawData) {
                String ngay = row.get("ngay_dang_ky_lop").toString(); // hoặc "Ngay" tùy SQL trả về
                Integer luotDangKy = Integer.parseInt(row.get("so_lop_duoc_dang_ky").toString());

                labels.add(ngay);
                values.add(luotDangKy);
            }

            // Đóng gói dữ liệu JSON gửi về frontend
            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", labels);
            chartData.put("data", values);

            // ---- END NEW

            // Map<String, List<?>> chartData = parseSheetDataToChartData(sheetData, "Ngày
            // đăng ký", "Số lượt đăng ký",
            // true);
            return ResponseEntity.ok(chartData);
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Lỗi khi đọc dữ liệu từ Google Sheet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi kết nối hoặc đọc dữ liệu từ Google Sheet.");
        } catch (IllegalArgumentException e) { // Bắt lỗi từ hàm parse
            System.err.println("Lỗi khi parse dữ liệu biểu đồ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dữ liệu không hợp lệ để tạo biểu đồ: " + e.getMessage()); // Có thể trả về message của
            // exception
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi không mong muốn.");
        }
    }

    // Bảo Như làm chính, Minh Long làm phần lọc bổ sung
    @ResponseBody
    @GetMapping("/chart-data/luotdangkygio")
    public ResponseEntity<?> getHourlyRegistrationStatsForChart(
            @RequestParam(required = false, defaultValue = "") String monHoc,
            @RequestParam(required = false, defaultValue = "") String hocKy,
            Model model) {
        try {
            String spreadsheetId = googleSheetService.SPREADSHEET_ID;
            List<List<Object>> sheetData = googleSheetService.readSheetData(spreadsheetId,
                    HOURLY_STATS_RANGE_FOR_CHART);

            // NEW ADDED BY MINH LONG NGUYEN
            // Gọi service xử lý SQL, đã lọc sẵn theo monHoc & hocKy nếu có
            List<Map<String, Object>> rawData = dashboardService.theoDoiSoLuotDKTheoGioFilter(monHoc, hocKy);

            // Duyệt dữ liệu để lấy nhãn (ngày) và giá trị (lượt đăng ký)
            List<String> labels = new ArrayList<>();
            List<Integer> values = new ArrayList<>();

            for (Map<String, Object> row : rawData) {
                String ngay = row.get("gio_dang_ky_lop").toString(); // hoặc "Gio" tùy SQL trả về
                Integer luotDangKy = Integer.parseInt(row.get("so_luot_duoc_dang_ky").toString());

                labels.add(ngay);
                values.add(luotDangKy);
            }

            // Đóng gói dữ liệu JSON gửi về frontend
            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", labels);
            chartData.put("data", values);

            // ---- END NEW

            // Map<String, List<?>> chartData = parseSheetDataToChartData(sheetData, "Thời
            // gian đăng Ký",
            // "Số lượt đăng ký", true);
            return ResponseEntity.ok(chartData);

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Lỗi khi đọc dữ liệu từ Google Sheet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi kết nối hoặc đọc dữ liệu từ Google Sheet.");
        } catch (IllegalArgumentException e) { // Bắt lỗi từ hàm parse
            System.err.println("Lỗi khi parse dữ liệu biểu đồ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dữ liệu không hợp lệ để tạo biểu đồ: " + e.getMessage()); // Có thể trả về message của
            // exception
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi không mong muốn.");
        }
    }

    // đóng mở trang đăng kí học phần
    @GetMapping("/motrangdkhp")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registration", new DongMoDKHP());
        return "motrangdkhp";
    }

    @PostMapping("/motrangdkhp")
    public ResponseEntity<String> setRegistrationPeriod(@RequestParam String maHK,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam boolean enable) {
        maHK = maHK.trim();
        if (start == null || end == null || start.isEmpty() || end.isEmpty()) {
            return ResponseEntity.badRequest().body("Ngày bắt đầu/kết thúc không được để trống!");
        }
        try {
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate = LocalDateTime.parse(end);
            dkhpService.datLichDKHP(maHK, enable, startDate, endDate); // gọi đến hàm đặt lịch đăng kí

        } catch (DateTimeParseException e) { // kiểm tra lỗi định dạng ngày tháng hay không
            return ResponseEntity.badRequest().body("Ngày tháng không đúng định dạng! Vui lòng nhập lại.");
        }

        return ResponseEntity.ok("Thời gian đăng ký đã được cập nhật cho học kỳ: " + maHK);
    }

    @GetMapping("/status/{maHK}") // trạng thái của học kì, đóng hay mở
    public ResponseEntity<Boolean> trangThaiDKHPs(@PathVariable String maHK) {
        return ResponseEntity.ok(dkhpService.moDKHP(maHK)); // nếu ok mở chặn đăng kí học phần
    }

    @GetMapping("/export/lophoc/{maLop}")
    public void exportSinhVienTheoLop(
            @PathVariable String maLop,
            HttpServletResponse response) {
        try {
            // 1. Lấy danh sách sinh viên từ DB
            List<SinhVien> danhSachSV = dkhpService.getSinhVienDangKy(maLop);
            // Chuyển đổi List<SinhVien> thành List<List<Object>>
            List<Object> headers = Arrays.asList("STT", "MSSV", "Họ và tên"); // Định nghĩa headers
            List<List<Object>> dataForSheet = new ArrayList<>();
            int stt = 1; // đặt số thứ tự cho bảng
            if (danhSachSV != null) {
                for (SinhVien sv : danhSachSV) {
                    dataForSheet.add(Arrays.asList(
                            stt++,
                            sv.getMaSV(),
                            sv.getHoTen()

                    ));
                }
            }

            // 3. Ghi dữ liệu (bao gồm cả header) lên sheet export cố định
            googleSheetService.prepareDataOnExportSheet(
                    DSSV_EXPORT_SPREADSHEET_ID, // << Truyền ID của Spreadsheet export
                    DSSV_EXPORT_TARGET_SHEET_NAME, // << Truyền tên Sheet trong Spreadsheet đó
                    dataForSheet, // Dữ liệu SV
                    headers // Headers
            );

            // 4. Tải file Google Sheet (mà sheet cố định vừa được cập nhật) về dưới dạng
            // Excel
            String downloadFileName = "DSSV_" + maLop.replace(".", "_").replace("/", "_");
            googleSheetService.downloadExportSheetAsExcel(
                    DSSV_EXPORT_SPREADSHEET_ID,
                    downloadFileName,
                    response);

        } catch (IOException e) {
            System.err.println("Lỗi I/O khi xuất file cho mã lớp " + maLop + ": " + e.getMessage());
            e.printStackTrace();
            // Xử lý lỗi và thông báo cho client
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // ...
        } catch (Exception e) { // Bắt các lỗi khác từ Google API hoặc logic
            System.err.println("Lỗi không xác định khi xuất file cho mã lớp " + maLop + ": " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // ...
        }
    }

     @Autowired
    private SinhVienRepository svRepo;
    
    // lấy sinh viên từ dtb
    @GetMapping("/sinhvien")
    public String showSinhVienPage(@RequestParam(name = "maSV", required = false) String maSV,
            @RequestParam(name = "hoTenSV", required = false) String hoTenSV,
            Model model) {

        List<SinhVien> danhSach;
        if ((maSV != null && !maSV.isEmpty()) || (hoTenSV != null && !hoTenSV.isEmpty())) {
            danhSach = adminService.searchSinhVien(maSV, hoTenSV); // Gọi hàm tìm kiếm
            model.addAttribute("isSearch", true); // Để biết đang ở trạng thái tìm kiếm
        } else {
            danhSach = adminService.getDanhSachSV(); // Lấy toàn bộ danh sách
            model.addAttribute("isSearch", false);
        }
        model.addAttribute("sinhVienList", danhSach);
        return "sinhvien";
    }
     
    @GetMapping("/themsinhvien")
    public String showThemSinhVienForm(Model model) {
        SinhVien sinhVien = new SinhVien(); // tạo form trống
        sinhVien.setTinhTrang("Đang học");
        model.addAttribute("sinhVien", sinhVien);
        return "themsinhvien"; 
    }
    // Thêm sinh viên mới
    @PostMapping("/themsinhvien")
    public String addSinhVien(@Valid @ModelAttribute("sinhVien") SinhVien sinhVien, BindingResult result,
            RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            result.getFieldErrors().forEach(error -> {
                System.err
                        .println("Lỗi validation tại trường '" + error.getField() + "': " + error.getDefaultMessage());
            });
            // Nếu có lỗi validation, trả về lại form để hiển thị lỗi
            return "themsinhvien";
        }
        System.out.println("Đã nhận từ form: " + sinhVien.getMaSV());
        boolean success = adminService.addSinhVien(sinhVien);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Thêm sinh viên thành công!");
        } else {
           
            model.addAttribute("error", "Mã sinh viên đã tồn tại!");
            model.addAttribute("sinhVien", sinhVien); // Đưa lại dữ liệu vào model
            return "themsinhvien"; // Trả về lại trang themsinhvien
        }
        return "redirect:/admin/sinhvien"; // Chuyển về trang danh sách
    }
    // Xóa sinh viên
    @PostMapping("/sinhvien/delete/{MaSV}")
    public String xoaSinhVien(@ModelAttribute SinhVien sinhVien, RedirectAttributes redirectAttributes) {
        System.out.println("Đã nhận từ form: " + sinhVien.getMaSV());
        try {
            adminService.deleteSinhVien(sinhVien);
            redirectAttributes.addFlashAttribute("message", "Xóa sinh viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa sinh viên thất bại!");
        }
        return "redirect:/admin/sinhvien"; 
    }

    // Sửa thông tin sinh viên
    @PutMapping("/sinhvien/suasinhvien/{maSV}")
    public ResponseEntity<Boolean> updateSinhVien(@PathVariable String maSV, @RequestBody SinhVien sinhVien) {
        return ResponseEntity.ok(adminService.updateSinhVien(sinhVien));
    }

    // Hiển thị form sửa thông tin sinh viên
    @GetMapping("/sinhvien/suasinhvien/{maSV}")
    public String showEditSinhVienForm(@PathVariable String maSV, Model model) {
        SinhVien sinhVien = adminService.findByIdSV(maSV); 
        if (sinhVien == null) {
            return "redirect:/admin/sinhvien?error=notfound";
        } else {
            model.addAttribute("sinhVien", sinhVien);
            return "suasinhvien"; 
        }
    }
    // Cập nhật thông tin sinh viên
    @PostMapping("/sinhvien/suasinhvien/{maSV}")
    public String updateSinhVien(@Valid @ModelAttribute("sinhVien") SinhVien sinhVien, BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "suasinhvien";
        }
        boolean updated = adminService.updateSinhVien(sinhVien);
        if (updated) {
            redirectAttributes.addFlashAttribute("msg", "Cập nhật thành công!");
        } else {
            redirectAttributes.addFlashAttribute("dmm", "Cập nhật thất bại!");
        }
        return "redirect:/admin/sinhvien";
    }
    // Tìm kiếm sinh viên
    @GetMapping("/sinhvien/search")
    public String searchSinhVien(@RequestParam(required = false) String maSV,
            @RequestParam(required = false) String hoTenSV,
            Model model) {
        List<GiangVien> ketQua = adminService.searchGiangVien(maSV, hoTenSV);
        model.addAttribute("danhSachSinhVien", ketQua);
        return "admin/sinhvien";
    }

    /** QUẢN LÝ GIẢNG VIÊN **/
    @Autowired
    private GiangVienRepository gvRepository;

    @GetMapping("/giangvien")
    public String showGiangVienPage(@RequestParam(name = "maGV", required = false) String maGV,
                                    @RequestParam(name = "hoTenGV", required = false) String hoTenGV,
                                    Model model) {
        List<GiangVien> danhSach;
        if ((maGV != null && !maGV.isEmpty()) || (hoTenGV != null && !hoTenGV.isEmpty())) {
            danhSach = adminService.searchGiangVien(maGV, hoTenGV);
            model.addAttribute("isSearch", true);
        } else {
            danhSach = adminService.getDanhSachGiangVien();
            model.addAttribute("isSearch", false);
        }
        model.addAttribute("giangVienList", danhSach);
        return "giangvien";
    }

    @GetMapping("/themgiangvien")
    public String showThemGiangVienForm(Model model) {
        model.addAttribute("giangVien", new GiangVien());
        return "themgiangvien";
    }

    @PostMapping("/themgiangvien")
    public String addGiangVien(@Valid @ModelAttribute("giangVien") GiangVien giangVien, BindingResult result,
                               RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            return "themgiangvien";
        }

        System.out.println("Đã nhận từ form: " + giangVien.getMaGV());

        boolean isAdded = adminService.addGiangVien(giangVien);

        if (isAdded) {
            redirectAttributes.addFlashAttribute("message", "Thêm giảng viên thành công!");
        } else {
            model.addAttribute("error", "Mã giảng viên đã tồn tại!");
            model.addAttribute("giangVien", giangVien);
            return "themgiangvien";
        }
        return "redirect:/admin/giangvien";
    }

    @PostMapping("/deleteGV/{maGV}")
    public String xoaGiangVien(@ModelAttribute GiangVien giangVien, RedirectAttributes redirectAttributes) {
        System.out.println("Đã nhận từ form: " + giangVien.getMaGV());
        try {
            adminService.deleteGiangVien(giangVien);
            redirectAttributes.addFlashAttribute("message", "Xóa giảng viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa giảng viên thất bại!");
        }
        return "redirect:/admin/giangvien";
    }

    @GetMapping("/giangvien/suagiangvien/{maGV}")
    public String showEditGiangVienForm(@PathVariable String maGV, Model model) {
        GiangVien gv = adminService.findGVById(maGV);
        if (gv == null) {
            return "redirect:/admin/giangvien?error=notfound";
        } else {
            model.addAttribute("giangVien", gv);
            return "suagiangvien";
        }
    }

    @PostMapping("/giangvien/suagiangvien/{maGV}")
    public String updateGiangVien(@Valid @ModelAttribute("giangVien") GiangVien giangVien, BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "suagiangvien";
        }
        boolean updated = adminService.updateGiangVien(giangVien);
        if (updated) {
            redirectAttributes.addFlashAttribute("msg", "Cập nhật giảng viên thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại!");
        }
        return "redirect:/admin/giangvien";
    }

    @PutMapping("/giangvien/suagiangvien/{maGV}")
    public ResponseEntity<Boolean> updateGiangVien(@PathVariable String maGV, @RequestBody GiangVien giangVien) {
        return ResponseEntity.ok(adminService.updateGiangVien(giangVien));
    }

    @GetMapping("/giangvien/search")
    public String searchGiangVien(
            @RequestParam(required = false) String maGV,
            @RequestParam(required = false) String hoTenGV,
            Model model) {
        List<GiangVien> ketQua = adminService.searchGiangVien(maGV, hoTenGV);
        model.addAttribute("danhSachGiangVien", ketQua);
        return "admin/giangvien";
    }

}


    

    
    

    

            

    
    
        
        

    

    

    