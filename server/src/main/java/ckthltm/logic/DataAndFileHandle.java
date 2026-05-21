package ckthltm.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ckthltm.models.CanBo;
import ckthltm.models.PhongThi;
import ckthltm.models.result.PhanCongGiamSat;
import ckthltm.models.result.PhanCongGiamThi;

public class DataAndFileHandle {

    private static String buildPhanCongFileName(int caThi) {
        return "phan_cong_cathi_" + caThi + ".xlsx";
    }

    private static String buildGiamSatFileName(int caThi) {
        return "giam_sat_cathi_" + caThi + ".xlsx";
    }

    public static String createDanhSachPhanCong(
            String outputDir,
            List<CanBo> danhSachCanBo,
            List<PhanCongGiamThi> danhSachPhanCongGiamThi,
            int caThi) {
        String fileName = buildPhanCongFileName(caThi);
        String filePath = outputDir + File.separator + fileName;

        Workbook danhSachPhanCong = openOrCreateWorkbook(filePath);
        removeSheetIfExists(danhSachPhanCong, "PhanCong");

        Sheet sheet = danhSachPhanCong.createSheet("PhanCong");

        int lastCol = 5; // 0..5
        int tableHeaderStartRow = 6;
        int dataStartRow = 8;

        writeNationalHeader(
                sheet,
                danhSachPhanCong,
                lastCol,
                "DANH SÁCH PHÂN CÔNG GIÁM THỊ COI THI",
                "Phiên: Ca " + caThi + "  -  Ngày: " + todayDdMmYyyy());

        Map<String, CanBo> canBoMap = buildCanBoMap(danhSachCanBo);

        // Khởi tạo các Style
        CellStyle headerStyle = createHeaderStyle(danhSachPhanCong);
        CellStyle centerStyle = createCenterStyle(danhSachPhanCong);
        CellStyle leftStyle = createLeftStyle(danhSachPhanCong);

        // Tạo sẵn lưới ô cho Header (2 dòng, 0..5) để hiển thị viền đầy đủ khi merge
        Row row0 = sheet.createRow(tableHeaderStartRow);
        Row row1 = sheet.createRow(tableHeaderStartRow + 1);
        for (int i = 0; i <= 5; i++) {
            row0.createCell(i).setCellStyle(headerStyle);
            row1.createCell(i).setCellStyle(headerStyle);
        }

        // Gộp ô (Merge cells)
        sheet.addMergedRegion(new CellRangeAddress(tableHeaderStartRow, tableHeaderStartRow + 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(tableHeaderStartRow, tableHeaderStartRow + 1, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(tableHeaderStartRow, tableHeaderStartRow + 1, 2, 2));
        sheet.addMergedRegion(new CellRangeAddress(tableHeaderStartRow, tableHeaderStartRow, 3, 4));
        sheet.addMergedRegion(new CellRangeAddress(tableHeaderStartRow, tableHeaderStartRow + 1, 5, 5));

        // Đặt giá trị cho Header
        row0.getCell(0).setCellValue("STT");
        row0.getCell(1).setCellValue("Mã GV");
        row0.getCell(2).setCellValue("Họ và tên");
        row0.getCell(3).setCellValue("Giám thị");
        row0.getCell(5).setCellValue("Phòng thi");

        row1.getCell(3).setCellValue("Giám thị 1");
        row1.getCell(4).setCellValue("Giám thị 2");

        int offset = dataStartRow;
        int stt = 0;

        for (int i = 0; i < danhSachPhanCongGiamThi.size(); ++i) {
            PhanCongGiamThi phanCong = danhSachPhanCongGiamThi.get(i);

            String phongThi = phanCong.getPhongThi();

            CanBo giamThi1 = canBoMap.get(phanCong.getMaGiamThi1());
            CanBo giamThi2 = canBoMap.get(phanCong.getMaGiamThi2());

            // Giám thị 1
            Row giamThi1Row = sheet.createRow(offset + i * 2);
            createStyledCell(giamThi1Row, 0, String.valueOf(++stt), centerStyle);
            createStyledCell(giamThi1Row, 1, phanCong.getMaGiamThi1(), centerStyle);
            createStyledCell(giamThi1Row, 2, giamThi1 != null ? giamThi1.getHoTen() : "", leftStyle);
            createStyledCell(giamThi1Row, 3, "X", centerStyle);
            createStyledCell(giamThi1Row, 4, "", centerStyle);
            createStyledCell(giamThi1Row, 5, phongThi, centerStyle);

            // Giám thị 2
            Row giamThi2Row = sheet.createRow(offset + i * 2 + 1);
            createStyledCell(giamThi2Row, 0, String.valueOf(++stt), centerStyle);
            createStyledCell(giamThi2Row, 1, phanCong.getMaGiamThi2(), centerStyle);
            createStyledCell(giamThi2Row, 2, giamThi2 != null ? giamThi2.getHoTen() : "", leftStyle);
            createStyledCell(giamThi2Row, 3, "", centerStyle);
            createStyledCell(giamThi2Row, 4, "X", centerStyle);
            createStyledCell(giamThi2Row, 5, phongThi, centerStyle);
        }

        // Tự động căn chỉnh độ rộng cột
        for (int i = 0; i <= 5; i++) {
            sheet.autoSizeColumn(i, true);
        }

        saveWorkbook(danhSachPhanCong, filePath);

        return fileName;
    }

    public static String createDanhSachGiamSat(
            String outputDir,
            List<PhanCongGiamSat> danhSachGiamSat,
            int caThi) {
        String fileName = buildGiamSatFileName(caThi);
        String filePath = outputDir + File.separator + fileName;

        Workbook danhSachPhanCong = openOrCreateWorkbook(filePath);
        removeSheetIfExists(danhSachPhanCong, "GiamSat");

        Sheet sheet = danhSachPhanCong.createSheet("GiamSat");

        int lastCol = 3; // 0..3
        int tableHeaderRow = 6;
        int dataStartRow = 7;

        writeNationalHeader(
            sheet,
            danhSachPhanCong,
            lastCol,
            "DANH SÁCH GIÁM SÁT HÀNH LANG",
            "Phiên: Ca " + caThi + "  -  Ngày: " + todayDdMmYyyy());

        CellStyle headerStyle = createHeaderStyle(danhSachPhanCong);
        CellStyle centerStyle = createCenterStyle(danhSachPhanCong);
        CellStyle leftStyle = createLeftStyle(danhSachPhanCong);

        Row row0 = sheet.createRow(tableHeaderRow);
        String[] headers = { "STT", "Mã GV", "Họ và tên", "Phòng thi được giám sát" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int offset = dataStartRow;
        int stt = 0;

        if (danhSachGiamSat == null || danhSachGiamSat.isEmpty()) {
            System.out.println("Ca thi " + caThi + ": Không có cán bộ dư để xếp giám sát hành lang.");
        } else {
            for (int i = 0; i < danhSachGiamSat.size(); ++i) {
                PhanCongGiamSat phanCong = danhSachGiamSat.get(i);
                CanBo giamSat = phanCong.getCanBo();

                Row row = sheet.createRow(offset + i);

                createStyledCell(row, 0, String.valueOf(++stt), centerStyle);

                if (giamSat == null) {
                    createStyledCell(row, 1, "", centerStyle);
                    createStyledCell(row, 2, "", leftStyle);
                    createStyledCell(row, 3, formatPhongGiamSat(phanCong.getPhongThiList()), centerStyle);
                    continue;
                }

                createStyledCell(row, 1, giamSat.getMaGV(), centerStyle);
                createStyledCell(row, 2, giamSat.getHoTen(), leftStyle);
                createStyledCell(row, 3, formatPhongGiamSat(phanCong.getPhongThiList()), centerStyle);
            }
        }

        // Tự động căn chỉnh độ rộng cột
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i, true);
        }

        saveWorkbook(danhSachPhanCong, filePath);

        return fileName;
    }

    private static Workbook openOrCreateWorkbook(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            return new XSSFWorkbook();
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            return new XSSFWorkbook(fis);
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file Excel cũ: " + e.getMessage());
            return new XSSFWorkbook();
        }
    }

    private static void removeSheetIfExists(Workbook workbook, String sheetName) {
        int sheetIndex = workbook.getSheetIndex(sheetName);

        if (sheetIndex >= 0) {
            workbook.removeSheetAt(sheetIndex);
        }
    }

    private static void saveWorkbook(Workbook workbook, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu file: " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private static Map<String, CanBo> buildCanBoMap(List<CanBo> danhSachCanBo) {
        Map<String, CanBo> map = new HashMap<>();

        if (danhSachCanBo == null) {
            return map;
        }

        for (CanBo cb : danhSachCanBo) {
            map.put(cb.getMaGV(), cb);
        }

        return map;
    }

    public static List<CanBo> readCanBoCoiThi(String filePath) {
        List<CanBo> danhSachCanBo = new ArrayList<>();
        try {
            try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {
                Sheet sheet = workbook.getSheetAt(0);
                boolean first = true;
                for (Row row : sheet) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (isRowEmpty(row)) {
                        continue;
                    }
                    DataFormatter formatter = new DataFormatter();
                    int thuTu = Integer.parseInt(formatter.formatCellValue(row.getCell(0)));
                    String maGV = formatter.formatCellValue(row.getCell(1));
                    String hoTen = formatter.formatCellValue(row.getCell(2));
                    String ngaySinh = formatter.formatCellValue(row.getCell(3));
                    String donViCongTac = formatter.formatCellValue(row.getCell(4));
                    danhSachCanBo.add(new CanBo(thuTu, maGV, hoTen, ngaySinh, donViCongTac));
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return danhSachCanBo;
    }

    public static List<PhongThi> readPhongThi(String filePath) {
        List<PhongThi> danhSachPhongThi = new ArrayList<>();
        try {
            try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {
                Sheet sheet = workbook.getSheetAt(1);
                boolean first = true;
                for (Row row : sheet) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (isRowEmpty(row)) {
                        continue;
                    }
                    DataFormatter formatter = new DataFormatter();
                    int thuTu = Integer.parseInt(formatter.formatCellValue(row.getCell(0)));
                    String phongThi = formatter.formatCellValue(row.getCell(1));
                    String diaDiem = formatter.formatCellValue(row.getCell(2));
                    danhSachPhongThi.add(new PhongThi(thuTu, phongThi, diaDiem));
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return danhSachPhongThi;
    }

    public static boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static String formatPhongGiamSat(List<String> phongThiList) {
        if (phongThiList == null || phongThiList.isEmpty()) {
            return "không có";
        }

        if (phongThiList.size() == 1) {
            return phongThiList.get(0);
        }

        return "Từ " + phongThiList.get(0)
                + " đến "
                + phongThiList.get(phongThiList.size() - 1);
    }

    private static void createStyledCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static String todayDdMmYyyy() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static void writeNationalHeader(
            Sheet sheet,
            Workbook workbook,
            int lastCol,
            String title,
            String subTitle) {
        // Row 0: Quốc hiệu
        Row r0 = sheet.createRow(0);
        Cell c0 = r0.createCell(0);
        c0.setCellValue("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM");
        c0.setCellStyle(createNationalTitleStyle(workbook));
        mergeRow(sheet, 0, 0, lastCol);
        applyStyleToMergedRow(r0, lastCol, c0.getCellStyle());

        // Row 1: Khẩu hiệu
        Row r1 = sheet.createRow(1);
        Cell c1 = r1.createCell(0);
        c1.setCellValue("Độc lập - Tự do - Hạnh phúc");
        c1.setCellStyle(createNationalMottoStyle(workbook));
        mergeRow(sheet, 1, 0, lastCol);
        applyStyleToMergedRow(r1, lastCol, c1.getCellStyle());

        // Row 2: trống
        sheet.createRow(2);

        // Row 3: Tiêu đề chính
        Row r3 = sheet.createRow(3);
        Cell c3 = r3.createCell(0);
        c3.setCellValue(title);
        c3.setCellStyle(createMainTitleStyle(workbook));
        mergeRow(sheet, 3, 0, lastCol);
        applyStyleToMergedRow(r3, lastCol, c3.getCellStyle());

        // Row 4: Phiên/Ngày
        Row r4 = sheet.createRow(4);
        Cell c4 = r4.createCell(0);
        c4.setCellValue(subTitle);
        c4.setCellStyle(createSubTitleStyle(workbook));
        mergeRow(sheet, 4, 0, lastCol);
        applyStyleToMergedRow(r4, lastCol, c4.getCellStyle());

        // Row 5: trống
        sheet.createRow(5);
    }

    private static void mergeRow(Sheet sheet, int rowIndex, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, firstCol, lastCol));
    }

    private static void applyStyleToMergedRow(Row row, int lastCol, CellStyle style) {
        for (int col = 1; col <= lastCol; col++) {
            Cell cell = row.getCell(col);
            if (cell == null) {
                cell = row.createCell(col);
            }
            cell.setCellStyle(style);
        }
    }

    private static CellStyle createNationalTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createNationalMottoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setUnderline(Font.U_SINGLE);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createMainTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createSubTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        return style;
    }

    private static CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static CellStyle createLeftStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}