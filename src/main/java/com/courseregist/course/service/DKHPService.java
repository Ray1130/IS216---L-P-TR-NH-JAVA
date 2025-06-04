package com.courseregist.course.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.courseregist.course.DTO.HPDTO;
import com.courseregist.course.entity.DongMoDKHP;
import com.courseregist.course.entity.SinhVien;
import com.courseregist.course.repository.DKHPRepository;
import com.courseregist.course.repository.HPRepository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
// Xử lý nghiệp vụ khi người dùng đăng kí học phần
public class DKHPService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HPRepository hpRepository;
    @Autowired
    private DKHPRepository dkRepository;

    // lấy danh sách học phần kì này
    public List<HPDTO> getDanhSachHP() {
        return hpRepository.getDanhSachHP(); // đã gọi ở repo
    }

    // danh sách học phần đã đăng kí theo sinh viên
    public List<HPDTO> getDanhSachHPDaDangKy(String maSV) {
        return hpRepository.getDanhSachHPDK(maSV);
    }

    public int dangKyHocPhan(String maSV, String maHK, String maLop) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("DANGKY_HOCPHAN") // GỌI HÀM THỦ TỤC DANGKYHOCPHAN TRONG DTB
                .declareParameters( // CAC BIEN TUONG UNG
                        new SqlParameter("p_masv", Types.VARCHAR),
                        new SqlParameter("p_mahk", Types.VARCHAR),
                        new SqlParameter("p_malop", Types.VARCHAR),
                        new SqlOutParameter("p_result", Types.INTEGER));
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_masv", maSV);
        inParams.put("p_mahk", maHK);
        inParams.put("p_malop", maLop);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (Integer) result.get("p_result");
    }

    public String convertDangKyResult(int resultCode) {
        // những cái case này đã được đánh mã ở trong procedure và theo trigger
        return switch (resultCode) {
            case 1 -> "Đăng ký thành công";
            case -1 -> "Trùng môn đã học";
            case -2 -> "Bạn đã đăng ký lớp này rồi";
            case -3 -> "Chưa học học phần tiên quyết";
            case -4 -> "Lớp trùng lịch học";
            case -5 -> "Lớp đã đầy";
            case -6 -> "Đăng ký quá 24 tín chỉ";
            case -7 -> "Chưa đăng ký lớp thực hành";
            case -8 -> "Lỗi lớp lý thuyết / thực hành";
            default -> "Lỗi không xác định";
        };
    }

    public List<HPDTO> searchHP(String maLop, String tenMH) {
        // đề phòng trường hợp input thừa ký tự thì trim() để xóa khoảng trống
        String maLopTrimmed = (maLop != null) ? maLop.trim() : "";
        String tenMHTrimmed = (tenMH != null) ? tenMH.trim() : "";

        // nếu không nhập gì thì vẫn ra dữ liệu bình thường
        if (maLopTrimmed.isEmpty() && tenMHTrimmed.isEmpty()) {
            // Assuming repository has a method to get all
            return hpRepository.getDanhSachHP();
        }
        return hpRepository.searchHP(maLopTrimmed, tenMHTrimmed);
    }

    public boolean deleteHP(String maSV, String maLop) {
        if (maLop == null || maLop.isEmpty()) {
            return false;
        }
        return hpRepository.xoaDangKyHocPhan(maSV, maLop);
    }

    // Kiểm tra xem kỳ học có mở đăng ký không
    public boolean moDKHP(String maHK) {
        DongMoDKHP config = dkRepository.getTrangThaiDK(maHK); // xem nó mở không
        LocalDateTime now = LocalDateTime.now();
        return config.isTrangThai() && now.isAfter(config.getNgayBatDau()) && now.isBefore(config.getNgayKetThuc());
        // so sánh thời gian hiện tại với ngaybatdau, ngày kết thúc
    }
    // Admin có thể cập nhật trạng thái đăng ký

    public void datLichDKHP(String maHK, boolean isOpen, LocalDateTime startDate, LocalDateTime endDate) {
        String trimmedMaHK = maHK.trim();

        // Kiểm tra xem MaHK có "tồn tại" (đã có lịch đăng ký) trong DONGMO_DKHP hay
        // không
        if (dkRepository.checkMaHKExists(trimmedMaHK)) {
            // Nếu dkRepository.checkMaHKExists(trimmedMaHK) trả về TRUE:
            // Nghĩa là MaHK này ĐÃ CÓ một bản ghi trong DONGMO_DKHP => update thay vì
            // insert
            dkRepository.updateTrangThaiDK(trimmedMaHK, isOpen, startDate, endDate);
        } else {
            // Nghĩa là MaHK này CHƯA CÓ bản ghi nào trong DONGMO_DKHP.
            dkRepository.insertRegistration(trimmedMaHK, isOpen, startDate, endDate);
        }
    }

    // tự động đóng dkhp khi hết hạn
    @Scheduled(fixedRate = 60000) // Kiểm tra mỗi phút
    public void autoDisableRegistration() {
        // 1. Lấy danh sách các kỳ đăng ký đang có TrangThai = 1 (đang mở)
        List<DongMoDKHP> registrations = dkRepository.danhSachDangHoatDong();
        LocalDateTime now = LocalDateTime.now(); // Lấy thời gian hiện tại của server

        for (DongMoDKHP reg : registrations) {
            // 2. Với mỗi kỳ đang mở, kiểm tra xem thời gian hiện tại đã VƯỢT QUÁ
            // NgayKetThuc chưa
            if (now.isAfter(reg.getNgayKetThuc())) {
                // 3. Nếu đã vượt quá, gọi Repository để cập nhật TrangThai = 0 (đóng)
                // và giữ nguyên NgayBatDau, NgayKetThuc
                System.out.println("Tự động đóng ĐKHP cho mã HK: " + reg.getMaHK() +
                        " vì đã hết hạn vào lúc: " + reg.getNgayKetThuc());
                dkRepository.updateTrangThaiDK(reg.getMaHK(), false, reg.getNgayBatDau(), reg.getNgayKetThuc());
            }
        }
    }

    // kiểm tra có cái nào đang mở không
    public boolean isAnyRegistrationPeriodCurrentlyOpen() {
        List<DongMoDKHP> activePeriods = dkRepository.danhSachDangHoatDong(); // Lấy các kỳ có TrangThai = 1
        if (activePeriods.isEmpty()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        for (DongMoDKHP period : activePeriods) {
            // Chỉ cần một kỳ thỏa mãn là đủ
            if (period.isTrangThai() && now.isAfter(period.getNgayBatDau()) && now.isBefore(period.getNgayKetThuc())) {
                return true;
            }
        }
        return false; // Không có kỳ nào đang mở và trong thời gian
    }

    // lấy danh sách sinh viên đăng kí học phần
    public List<SinhVien> getSinhVienDangKy(String maLop) {
        return hpRepository.getSinhVienDangKy(maLop);

    }

}
