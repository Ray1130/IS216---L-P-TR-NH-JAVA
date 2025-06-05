package com.courseregist.course.controller;

import com.courseregist.course.service.GoogleSheetsUpdater;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/sheets")
public class GoogleSheetsController {
    private final GoogleSheetsUpdater googleSheetsUpdater;

    public GoogleSheetsController(GoogleSheetsUpdater googleSheetsUpdater) {
        this.googleSheetsUpdater = googleSheetsUpdater;
    }

    @PostMapping("/update")
    public String updateSheets() throws Exception {
        try {
            googleSheetsUpdater.updateGoogleSheet(); // Gọi phương thức cập nhật dữ liệu
            googleSheetsUpdater.updateHourlyClassStatsSheet();
            googleSheetsUpdater.updateDailyClassStatsSheet();
            return "Cập nhật Google Sheets thành công!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Có lỗi xảy ra khi cập nhật!";
        }
    }
}
