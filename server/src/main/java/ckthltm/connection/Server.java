package ckthltm.connection;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.SwingUtilities;

import ckthltm.interfaces.MessageCallback;
import ckthltm.dal.*;
import ckthltm.logic.*;
import ckthltm.models.*;
import ckthltm.models.result.*;

public class Server {
    private final String fileSavePath = "downloads";
    private final String outputDir = "outputs";
    private MessageSender messageSender = new MessageSender();
    private ServerSocket serverSocket;
    private Socket connectedSocket;
    private UI ui;

    private void refreshUiStats() {
        if (ui == null) {
            return;
        }
        try {
            int canBoCount = new CanBoDAO().getAll().size();
            int phongThiCount = new PhongThiDAO().getAll().size();
            int caThiCount = new YeuCauDAO().getAll().size();
            ui.setStats(canBoCount, phongThiCount, caThiCount);
        } catch (Exception ignored) {
        }
    }

    private class ReceivedCallback implements MessageCallback {

        @Override
        public void onTextMessageReceived(String message) {
            System.out.println("[SERVER] Nhận yêu cầu: " + message);
            try {
                // PARSE DATA
                int n = Integer.parseInt(message.split("n=")[1].split(",")[0].trim()); // Phòng thi
                int m = Integer.parseInt(message.split("m=")[1].split(",")[0].trim()); // Cán bộ
                int soCanBoGiamSat = m - 2 * n;

                System.out.println("[SERVER] Tham số: n=" + n + ", m=" + m + ", canBoGiamSat=" + soCanBoGiamSat);

                if (soCanBoGiamSat <= 0) {
                    throw new Exception("Can it nhat 1 can bo giam sat");
                }

                // LẤY DỮ LIỆU TỪ DB
                CanBoDAO canBoDAO = new CanBoDAO();
                List<CanBo> canBoFullList = canBoDAO.getAll();
                PhongThiDAO phongThiDAO = new PhongThiDAO();
                List<PhongThi> phongThiFullList = phongThiDAO.getAll();
                YeuCauDAO yeuCauDAO = new YeuCauDAO();
                List<YeuCau> yeuCauFullList = yeuCauDAO.getAll();

                if (m > canBoFullList.size()) {
                    throw new Exception("So can bo yeu cau lon hon so can bo trong he thong");
                }

                if (n > phongThiFullList.size()) {
                    throw new Exception("So phong thi yeu cau lon hon so phong thi trong he thong");
                }

                // TÍNH LINE
                int S = GenerateSchedule.getS(canBoFullList.size(), phongThiFullList.size());
                int Lmax = GenerateSchedule.getLmax(canBoFullList.size(), phongThiFullList.size());
                int sucChuaMotLine = Math.min(S, phongThiFullList.size());

                if (n > sucChuaMotLine) {
                    throw new Exception("So phong thi n vuot qua suc chua mot line. n=" + n + ", S=" + S);
                }

                int line = -1;
                int offset = 0;

                yeuCauFullList.sort((a, b) -> {
                    if (a.getLine() != b.getLine()) {
                        return Integer.compare(a.getLine(), b.getLine());
                    }
                    return Long.compare(a.getId(), b.getId());
                });

                int currentLine = 0;
                int soPhongThiDaDung = 0;

                for (YeuCau yc : yeuCauFullList) {
                    if (yc.getLine() != currentLine) {
                        if (sucChuaMotLine - soPhongThiDaDung >= n) {
                            line = currentLine;
                            offset = soPhongThiDaDung;
                            break;
                        }

                        currentLine = yc.getLine();
                        soPhongThiDaDung = 0;
                    }

                    soPhongThiDaDung += yc.getSoPhongThi();
                }

                if (line == -1) {
                    if (sucChuaMotLine - soPhongThiDaDung >= n) {
                        line = currentLine;
                        offset = soPhongThiDaDung;
                    } else {
                        line = currentLine + 1;
                        offset = 0;
                    }
                }

                if (line >= Lmax) {
                    throw new Exception("Khong con line hop le de tao ca thi moi");
                }

                System.out.println("[SERVER] Slot: line=" + line + ", offset=" + offset);

                // TẠO PHÂN CÔNG GIÁM THỊ
                List<PhanCongGiamThi> phanCongGiamThiFull = GenerateSchedule.generateGiamThiFull(line, phongThiFullList,
                        canBoFullList);
                List<PhanCongGiamThi> phanCongGiamThiPartition = phanCongGiamThiFull.subList(offset, offset + n);

                // TẠO PHÂN CÔNG GIÁM SÁT
                List<PhongThi> phongThiDuocSuDung = phongThiFullList.subList(offset, offset + n);
                List<CanBo> canBoConLai = GenerateSchedule.getCanBoConLai(phanCongGiamThiPartition, canBoFullList);
                if (soCanBoGiamSat > canBoConLai.size()) {
                    throw new Exception("Khong du can bo con lai de xep giam sat");
                }
                GiamSatDAO giamSatDAO = new GiamSatDAO();
                List<PhanCongGiamSat> phanCongGiamSatList = GenerateSchedule.generateGiamSat(
                        phongThiDuocSuDung,
                        canBoConLai,
                        soCanBoGiamSat, giamSatDAO);

                // GỬI KẾT QUẢ VỀ CLIENT
                int caThi = yeuCauFullList.size() + 1;
                String phanCongFileName = DataAndFileHandle.createDanhSachPhanCong(outputDir,
                        canBoFullList,
                        phanCongGiamThiPartition,
                        caThi);
                String giamSatFileName = DataAndFileHandle.createDanhSachGiamSat(
                        outputDir,
                        phanCongGiamSatList,
                        caThi);

                String phanCongFilePath = outputDir + File.separator + phanCongFileName;
                String giamSatFilePath = outputDir + File.separator + giamSatFileName;

                messageSender.sendFile(connectedSocket, phanCongFilePath, phanCongFileName);
                messageSender.sendFile(connectedSocket, giamSatFilePath, giamSatFileName);
                System.out.println("[SERVER] Đã gửi kết quả về client: " + phanCongFileName + ", " + giamSatFileName);

                // LƯU YÊU CẦU VÀO DB 
                YeuCau newYeuCau = new YeuCau(m, n, line);
                if (yeuCauDAO.insert(newYeuCau)) {
                    System.out.println("[SERVER] Đã lưu yêu cầu vào database.");
                } else {
                    System.err.println("[SERVER] Lỗi khi lưu yêu cầu vào database.");
                }

                // LƯU GIÁM SÁT VÀO DB
                giamSatDAO.insertAllPhanCongGiamSat(phanCongGiamSatList);

                refreshUiStats();
            } catch (Exception e) {
                System.err.println("[SERVER] Lỗi xử lý yêu cầu: " + e.getMessage());
            }
        }

        @Override
        public void onFileMessageReceived(String savedPath, String fileName) {
            System.out.println("[SERVER] Nhận file: " + fileName);
            if (fileName.equals("DanhSachCanBoCoiThi.xlsx")) {
                System.out.println("[SERVER] Bắt đầu xử lý dữ liệu từ Excel...");
                try {
                    // Xóa dữ liệu cũ trong DB
                    CanBoDAO canBoDAO = new CanBoDAO();
                    canBoDAO.deleteAll();
                    PhongThiDAO phongThiDAO = new PhongThiDAO();
                    phongThiDAO.deleteAll();
                    new YeuCauDAO().deleteAll();
                    new GiamSatDAO().deleteAll();

                    // Xử lý Cán Bộ
                    List<CanBo> danhSachCanBo = DataAndFileHandle.readCanBoCoiThi(savedPath);
                    int cbSize = (danhSachCanBo != null) ? danhSachCanBo.size() : 0;
                    int cbSuccess = 0;

                    if (cbSize > 0) {
                        for (CanBo cb : danhSachCanBo) {
                            if (canBoDAO.insert(cb))
                                cbSuccess++;
                        }
                    }
                    System.out.println("CanBo: " + cbSuccess + "/" + cbSize + " records inserted.");

                    // Xử lý Phòng Thi
                    List<PhongThi> danhSachPhongThi = DataAndFileHandle.readPhongThi(savedPath);
                    int ptSize = (danhSachPhongThi != null) ? danhSachPhongThi.size() : 0;
                    int ptSuccess = 0;

                    if (ptSize > 0) {
                        for (PhongThi pt : danhSachPhongThi) {
                            if (phongThiDAO.insert(pt))
                                ptSuccess++;
                        }
                    }
                    System.out.println("PhongThi: " + ptSuccess + "/" + ptSize + " records inserted.");
                    System.out.println("[SERVER] Hoàn tất cập nhật DB từ Excel.");

                    refreshUiStats();
                } catch (Exception e) {
                    System.err.println("[SERVER] Lỗi xử lý Excel: " + e.getMessage());
                }
            }
        }

    }

    public void start(int port) {
        try {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    ui = new UI();
                    ui.display();
                });
            } catch (Exception e) {
                System.err.println("[SERVER] Cannot start Swing UI: " + e.getMessage());
            }

            refreshUiStats();

            this.serverSocket = new ServerSocket(port);
            System.out.println("Server start at port: " + port);
            connectedSocket = serverSocket.accept();
            MessageListener messageListener = new MessageListener(connectedSocket, fileSavePath, new ReceivedCallback());
            messageListener.start();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}