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
            System.out.println("[SERVER] NHẬN YÊU CẦU MỚI: " + message);
            try {
                // PARSE DATA
                int n = Integer.parseInt(message.split("n=")[1].split(",")[0].trim()); // Phòng thi
                int m = Integer.parseInt(message.split("m=")[1].split(",")[0].trim()); // Cán bộ
                System.out.println("[SERVER] Parse Data: n(Phòng)=" + n + ", m(Cán bộ)=" + m);
                int soCanBoGiamSat = m - 2 * n;

                if (soCanBoGiamSat < 0) {
                    throw new Exception("Khong du can bo: can it nhat 2 can bo cho moi phong thi");
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

                System.out.println("[SERVER] S=" + S + ", Lmax=" + Lmax + ", line=" + line + ", offset=" + offset);

                // TẠO PHÂN CÔNG GIÁM THỊ
                List<PhanCongGiamThi> phanCongGiamThiFull = GenerateSchedule.generateGiamThiFull(line, phongThiFullList,
                        canBoFullList);
                System.out.println("[SERVER] Phân công giám thị full đã được tạo.");
                List<PhanCongGiamThi> phanCongGiamThiPartition = phanCongGiamThiFull.subList(offset, offset + n);
                System.out.println("[SERVER] Phân công giám thị đã được tạo.");

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
                System.out.println("[SERVER] Phân công giám sát đã được tạo.");

                // GỬI KẾT QUẢ VỀ CLIENT
                int caThi = yeuCauFullList.size() + 1;
                String fileName = DataAndFileHandle.createDanhSachPhanCong(outputDir,
                        canBoFullList,
                        phanCongGiamThiPartition,
                        caThi);
                DataAndFileHandle.createDanhSachGiamSat(
                        outputDir,
                        phanCongGiamSatList,
                        caThi);
                String filePath = outputDir + File.separator + fileName;
                messageSender.sendFile(connectedSocket, filePath, fileName);
                System.out.println("[SERVER] Kết quả đã được gửi về client.");

                // LƯU YÊU CẦU VÀO DB 
                YeuCau newYeuCau = new YeuCau(m, n, line);
                if (yeuCauDAO.insert(newYeuCau)) {
                    System.out.println("[SERVER] Yêu cầu đã được lưu vào database.");
                } else {
                    System.err.println("[SERVER] Lỗi khi lưu yêu cầu vào database.");
                }

                // LƯU GIÁM SÁT VÀO DB
                giamSatDAO.insertAllPhanCongGiamSat(phanCongGiamSatList);

                refreshUiStats();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        @Override
        public void onFileMessageReceived(String savedPath, String fileName) {
            System.out.println("Received file: " + fileName);
            if (fileName.equals("DanhSachCanBoCoiThi.xlsx")) {
                System.out.println("===> Processing: " + fileName);
                try {
                    // Xóa dữ liệu cũ trong DB
                    CanBoDAO canBoDAO = new CanBoDAO();
                    canBoDAO.deleteAll();
                    System.out.println("Deleted records from CanBo table.");
                    PhongThiDAO phongThiDAO = new PhongThiDAO();
                    phongThiDAO.deleteAll();
                    System.out.println("Deleted records from PhongThi table.");
                    new YeuCauDAO().deleteAll();
                    System.out.println("Deleted records from YeuCau table.");
                    new GiamSatDAO().deleteAll();
                    System.out.println("Deleted records from GiamSat table.");

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
                    System.out.println("===> Done.");

                    refreshUiStats();
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
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