package com.courseregist.course.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.courseregist.course.repository.AdminDashBoardRepository;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GoogleSheetsUpdater {
    private final AdminDashBoardRepository adminRepository;
    private final GoogleSheetsService googleSheetsService;
    private static final String SHEET_NAME = "Trang tính1";
    private static final String HOURLY_CLASS_STATS_SHEET_NAME = "Trang tính2";
    private static final String DAILY_CLASS_STATS_SHEET_NAME = "Trang tính3";
    private static final String HEADER_NGANH = "Ngành";
    private static final String HEADER_SO_LUONG_SV = "Số Sinh Viên";
    private static final String HEADER_THOI_GIAN_LOP = "Thời gian đăng ký";
    private static final String HEADER_NGAY_LOP = "Ngày đăng ký";
    private static final String HEADER_SO_LOP_DK = "Số lượt đăng ký";

    public GoogleSheetsUpdater(AdminDashBoardRepository adminnRepository, GoogleSheetsService googleSheetsService) {
        this.adminRepository = adminnRepository;
        this.googleSheetsService = googleSheetsService;
    }

    @PostConstruct // <<< Gọi updateGoogleSheet() ngay khi ứng dụng khởi động!
    public void init() throws Exception {
        try {
            updateGoogleSheet(); // Cập nhật cho "Trang tính1"
            updateHourlyClassStatsSheet();
            updateDailyClassStatsSheet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 3600000) // Cập nhật mỗi 1 giờ (3600000 ms)
    public void autoUpdateGoogleSheet() throws Exception {
        updateGoogleSheet(); // cập nhật dữ liệu lên google sheet
    }

    public void updateGoogleSheet() throws Exception {
        Sheets sheetsService = null;
        try {
            sheetsService = googleSheetsService.getSheetsService();
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Ném lại để autoUpdateGoogleSheet() có thể bắt
        }
        System.out.println("Đã lấy được Sheets service.");
        String spreadsheetId = GoogleSheetsService.SPREADSHEET_ID;
        String writeRange = SHEET_NAME + "!A1";
        List<List<Object>> values = new ArrayList<>();
        values.add(List.of(HEADER_NGANH, HEADER_SO_LUONG_SV)); // thêm các header vào table, ở đây nó nhằm trong A1 đổ
                                                               // sang

        List<Map<String, Object>> data = null;
        try {
            data = adminRepository.laySinhVienTheoNganh();
        } catch (Exception e) {
            System.err.println("LỖI khi gọi adminRepository.laySinhVienTheoNganh(): " + e.getMessage());
            e.printStackTrace();
            throw e; // Ném lại
        }
        if (data != null) {
            for (Map<String, Object> row : data) {
                values.add(List.of(row.get("MaNganh"), row.get("so_luong_sinh_vien"))); // so key với tên thuộc tính
                                                                                        // trong database
            }
        }
        if (values.size() <= 1 && (data == null || data.isEmpty())) {
            System.out.println(
                    "Không có dữ liệu ngành từ DB để cập nhật sẽ ghi mỗi header");
        }

        ValueRange body = new ValueRange().setValues(values); // set giá trị
        try {
            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, writeRange, body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (Exception e) {
            System.err.println("LỖI khi gọi API update của Google Sheets: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("✅ Cập nhật Google Sheets thành công!"); // Log 8
    }

    // cập nhật thống kê số lớp đăng ký theo giờ
    @Scheduled(cron = "0 15 * * * ?") // Chạy vào phút 15 của mỗi giờ
    public void autoUpdateHourlyClassRegistrationStats() {
        try {
            updateHourlyClassStatsSheet();
        } catch (Exception e) {
            System.err.println(
                    "LỖI trong quá trình tự động cập nhật thống kê số lớp đăng ký theo giờ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // update theo giờ
    public void updateHourlyClassStatsSheet() throws Exception {
        String spreadsheetId = GoogleSheetsService.SPREADSHEET_ID;
        String writeRange = HOURLY_CLASS_STATS_SHEET_NAME + "!A1";

        List<List<Object>> values = new ArrayList<>();
        values.add(List.of(HEADER_THOI_GIAN_LOP, HEADER_SO_LOP_DK));

        List<Map<String, Object>> hourlyData = adminRepository.theoDoiSoLuotDKTheoGio();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");

        if (hourlyData != null) {
            for (Map<String, Object> row : hourlyData) {
                Object timestampObj = row.get("GIO_DANG_KY_LOP");
                Object countObj = row.get("SO_LOP_DUOC_DANG_KY");

                String formattedTime = "N/A";
                if (timestampObj instanceof Timestamp) {
                    formattedTime = ((Timestamp) timestampObj).toLocalDateTime().format(formatter);
                }
                long count = 0;
                if (countObj instanceof Number) {// đếm theo số giờ
                    count = ((Number) countObj).longValue();
                }
                values.add(List.of(formattedTime, count));
            }
        }

        if (values.size() > 1 || (values.size() == 1 && (hourlyData == null || hourlyData.isEmpty()))) {
            googleSheetsService.updateSheetData(writeRange, values, "USER_ENTERED");
        } else {
            System.out.println("Không có dữ liệu thống kê số lớp đăng ký theo giờ để cập nhật.");
        }
    }

    // thống kê đăng kí theo ngày
    @Scheduled(cron = "0 5 0 * * ?")
    public void autoUpdateDailyClassRegistrationStats() {
        try {
            updateDailyClassStatsSheet();
        } catch (Exception e) {
            System.err.println(
                    "LỖI trong quá trình tự động cập nhật thống kê số lớp đăng ký theo NGÀY: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateDailyClassStatsSheet() throws Exception {
        String spreadsheetId = GoogleSheetsService.SPREADSHEET_ID;
        String writeRange = DAILY_CLASS_STATS_SHEET_NAME + "!A1";

        List<List<Object>> values = new ArrayList<>();
        values.add(List.of(HEADER_NGAY_LOP, HEADER_SO_LOP_DK));

        List<Map<String, Object>> dailyData = adminRepository.theoDoiSoLuotDKTheoNgay();
        System.out.println(
                "Dữ liệu lớp theo ngày từ DB: " + (dailyData != null ? dailyData.size() + " bản ghi." : "null"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (dailyData != null) {
            for (Map<String, Object> row : dailyData) {
                Object timestampObj = row.get("NGAY_DANG_KY_LOP");
                Object countObj = row.get("SO_LUOT_DUOC_DANG_KY");

                String formattedTime = "N/A";
                if (timestampObj instanceof Timestamp) {
                    formattedTime = ((Timestamp) timestampObj).toLocalDateTime().format(formatter);
                }
                long count = 0;
                if (countObj instanceof Number) {
                    count = ((Number) countObj).longValue();
                }
                values.add(List.of(formattedTime, count));
            }
        }

        if (values.size() > 1 || (values.size() == 1 && (dailyData == null || dailyData.isEmpty()))) {
            googleSheetsService.updateSheetData(writeRange, values, "USER_ENTERED");
            System.out.println("✅ Cập nhật thống kê số lớp đăng ký theo ngày thành công! Số dòng: " + values.size());
        } else {
            System.out.println("Không có dữ liệu thống kê số lớp đăng ký theo giờ để cập nhật.");
        }
    }
}
