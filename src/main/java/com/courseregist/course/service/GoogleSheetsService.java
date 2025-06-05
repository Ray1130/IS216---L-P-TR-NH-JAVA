package com.courseregist.course.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@Service
public class GoogleSheetsService {
    public static final String SPREADSHEET_ID = "1Fx589i02ULQlNSvWOcJuShiYkKSGfNNZ5z71VFwBsVs"; // id file sheet lưu dữ
                                                                                                // liệu
    private static final String CREDENTIALS_FILE_PATH = "doan.json"; // Tên file credentials trong resources
    private static final String APPLICATION_NAME = "Course Registration App"; // Tên ứng dụng
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // đọc data từ sheet
    public List<List<Object>> readSheetData(String spreadsheetId, String range)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values() // Truy cập vào tài nguyên "spreadsheets" của API.
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    private Sheets sheetsServiceInstance; // Biến để cache Sheets service
    private Drive driveServiceInstance; // Biến để cache Drive service
    private NetHttpTransport httpTransport; // Biến để cache HTTP Transport

    /**
     * Phương thức này sẽ được gọi sau khi bean được khởi tạo.
     * Nó chuẩn bị HTTP Transport một lần.
     */
    @PostConstruct
    private void initialize() throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        System.out.println("Google HTTP Transport initialized.");
    }

    // SCOPES cần thiết cho cả Sheets và Drive
    private static final List<String> SCOPES = Arrays.asList(
            SheetsScopes.SPREADSHEETS, // Quyền đọc/ghi Google Sheets
            DriveScopes.DRIVE_FILE // Quyền export/download file từ Google Drive
    );

    // method này để thao tác với google sheet
    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        if (sheetsServiceInstance == null) {
            System.out.println("Creating new Sheets service instance...");
            InputStream in = GoogleSheetsService.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new IOException("LỖI NGHIÊM TRỌNG: Không tìm thấy file credentials '" + CREDENTIALS_FILE_PATH
                        + "' trên classpath!");
            }

            GoogleCredentials credentials;
            try {
                credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES); // xác thực quyền google cho truy
                                                                                     // cập chưa?
            } finally {
                if (in != null)
                    in.close();
            }

            sheetsServiceInstance = new Sheets.Builder(
                    this.httpTransport, // Sử dụng HTTP Transport đã được khởi tạo
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            System.out.println("Sheets service instance created.");
        }
        return sheetsServiceInstance;
    }

    /**
     * Lấy một instance c Drive service đã được ủy quyền.ủa
     *
     * @return một Drive service đã được ủy quyền.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    // cái trên cấp quyền cho sheet, cái này cho drive để drive có thể truy cập vào
    // rồi xuất file
    public Drive getDriveServiceInternal() throws IOException, GeneralSecurityException {
        if (driveServiceInstance == null) {
            InputStream in = GoogleSheetsService.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH); // lấy
                                                                                                                    // đường
                                                                                                                    // dẫn
                                                                                                                    // xác
                                                                                                                    // thực
            if (in == null) {
                throw new IOException("LỖI NGHIÊM TRỌNG: Không tìm thấy file credentials '" + CREDENTIALS_FILE_PATH
                        + "' trên classpath!");
            }

            GoogleCredentials credentials;
            try {
                credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);
            } finally {
                if (in != null)
                    in.close();
            }

            driveServiceInstance = new Drive.Builder(
                    this.httpTransport, // Sử dụng HTTP Transport đã được khởi tạo
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            System.out.println("Drive service instance created.");
        }
        return driveServiceInstance;
    }

    /**
     * Tạo và trả về một Sheets service đã được ủy quyền.
     * 
     * @return một Sheets service đã được ủy quyền.
     * @throws IOException              Nếu không thể đọc file credentials hoặc lỗi
     *                                  mạng.
     * @throws GeneralSecurityException Nếu có lỗi bảo mật khi thiết lập transport.
     */

    /**
     * Thêm dữ liệu vào cuối của một sheet.
     * 
     * @param sheetNameAndRange Ví dụ: "Sheet1!A1" hoặc chỉ "Sheet1" (API sẽ tìm
     *                          dòng trống đầu tiên)
     * @param values            Dữ liệu để thêm.
     * @param valueInputOption  Cách dữ liệu được diễn giải ("RAW" hoặc
     *                          "USER_ENTERED").
     * @throws Exception Nếu có lỗi khi gọi API.
     */
    // thêm dữ liệu
    public void appendData(String sheetNameAndRange, List<List<Object>> values, String valueInputOption)
            throws Exception {
        Sheets sheetsService = getSheetsService();
        ValueRange body = new ValueRange().setValues(values); // lấy dữ liệu và set vào cái ô đó

        sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, sheetNameAndRange, body)
                .setValueInputOption(valueInputOption) // "RAW" hoặc "USER_ENTERED"
                .execute();
        System.out.println("Dữ liệu đã được append vào: " + sheetNameAndRange);
    }

    /**
     * Ghi/Ghi đè dữ liệu vào một vùng cụ thể của sheet.
     * 
     * @param rangeToUpdate    Ví dụ: "Sheet1!A1"
     * @param values           Dữ liệu để ghi.
     * @param valueInputOption Cách dữ liệu được diễn giải ("RAW" hoặc
     *                         "USER_ENTERED").
     * @throws Exception Nếu có lỗi khi gọi API.
     */
    // nó sẽ ghi đè dữ liệu cũ để update dữ liệu mới lên
    public void updateSheetData(String rangeToUpdate, List<List<Object>> values, String valueInputOption)
            throws Exception {
        Sheets sheetsService = getSheetsService();
        ValueRange body = new ValueRange().setValues(values);

        sheetsService.spreadsheets().values().update(SPREADSHEET_ID, rangeToUpdate, body)
                .setValueInputOption(valueInputOption) // "RAW" hoặc "USER_ENTERED"
                .execute();
        System.out.println("Đã cập nhật dữ liệu vào range: " + rangeToUpdate);
    }

    /**
     * Xóa nội dung của một vùng cụ thể trong sheet.
     * 
     * @param rangeToClear Ví dụ: "Sheet1!A2:Z"
     * @throws Exception Nếu có lỗi khi gọi API.
     */
    public void clearSheetRange(String rangeToClear) throws Exception {
        Sheets sheetsService = getSheetsService();
        ClearValuesRequest requestBody = new ClearValuesRequest();

        sheetsService.spreadsheets().values().clear(SPREADSHEET_ID, rangeToClear, requestBody).execute();
        System.out.println("Đã xóa dữ liệu trong range: " + rangeToClear);
    }

    public void exportData(List<List<Object>> values) throws Exception {
        // Giả sử append vào Sheet1, bắt đầu từ A1 (API sẽ tìm dòng trống)
        // và sử dụng valueInputOption là "RAW"
        appendData("Sheet1!A1", values, "RAW");
    }

    // Đưa dữ liệu để export
    public void prepareDataOnExportSheet(
            String exportSpreadsheetId, // ID của Spreadsheet DÙNG ĐỂ EXPORT
            String sheetName, // Tên sheet trong Spreadsheet export đó
            List<List<Object>> data,
            List<Object> headers)
            throws IOException, GeneralSecurityException {

        Sheets sheetsService = getSheetsService(); // Lấy service
        // mỗi lần xuất dữ liệu là 1 lần xóa dữ liệu đi update lại để dùng 1 google
        // sheet cho nhiều đối tượng

        // 1. XÓA SẠCH DỮ LIỆU CŨ TRÊN SHEET ĐANG DÙNG TỚI
        String clearRange = sheetName + "!A1:Z"; // Xóa từ A1 đến hết cột Z
        ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
        System.out.println("Đang xóa dữ liệu cũ trên: " + sheetName + " của Spreadsheet ID: " + exportSpreadsheetId);
        sheetsService.spreadsheets().values()
                .clear(exportSpreadsheetId, clearRange, clearValuesRequest)
                .execute();
        System.out.println("Đã xóa xong dữ liệu cũ.");

        // 2. CHUẨN BỊ DỮ LIỆU MỚI ĐỂ GHI
        List<List<Object>> valuesToWrite = new ArrayList<>();
        if (headers != null && !headers.isEmpty()) {
            valuesToWrite.add(headers); // Thêm headers vào dòng đầu tiên
        }
        if (data != null && !data.isEmpty()) {
            valuesToWrite.addAll(data); // Thêm dữ liệu sinh viên
        }

        if (valuesToWrite.isEmpty()) {
            System.out.println("Không có dữ liệu mới để ghi lên sheet: " + sheetName);
            return; // Không làm gì nếu không có dữ liệu
        }

        // 3. GHI DỮ LIỆU MỚI LÊN SHEET (BẮT ĐẦU TỪ A1)
        String writeRange = sheetName + "!A1";
        ValueRange body = new ValueRange().setValues(valuesToWrite);
        System.out.println("Đang ghi dữ liệu mới (" + valuesToWrite.size() + " dòng) lên: " + sheetName);
        sheetsService.spreadsheets().values()
                .update(exportSpreadsheetId, writeRange, body)
                .setValueInputOption("USER_ENTERED") // Để Google Sheets tự parse kiểu dữ liệu
                .execute();
        System.out.println("Đã ghi xong dữ liệu mới.");
    }

    /**
     * Tải spreadsheet cụ thể (dùng cho export) dưới dạng file Excel.
     * Spreadsheet này đã được cập nhật dữ liệu mới từ prepareDataOnExportSheet.
     */

    public void downloadExportSheetAsExcel(
            String exportSpreadsheetId, // ID của Spreadsheet DÙNG ĐỂ EXPORT
            String downloadFileName,
            HttpServletResponse response)
            throws IOException, GeneralSecurityException {

        Drive driveService = getDriveServiceInternal(); // Lấy service
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        response.setContentType(mimeType);
        // định dạng tải xuống
        String safeFileName = downloadFileName.replaceAll("[^a-zA-Z0-9._-]", "_") + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + safeFileName + "\"");

        try (OutputStream outputStream = response.getOutputStream()) {
            System.out.println("Bắt đầu export file từ Google Drive...");
            driveService.files().export(exportSpreadsheetId, mimeType)
                    .executeMediaAndDownloadTo(outputStream);
            outputStream.flush();

            System.out.println("Đã tải file thành công.");
        } catch (IOException e) {
            System.err.println("Lỗi khi tải file Excel: " + e.getMessage());
            throw e;
        }
    }

}