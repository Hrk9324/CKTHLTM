package ckthltm.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ckthltm.dal.GiamSatDAO;
import ckthltm.models.CanBo;
import ckthltm.models.PhongThi;
import ckthltm.models.result.PhanCongGiamSat;
import ckthltm.models.result.PhanCongGiamThi;

public class GenerateSchedule {
    public static List<PhanCongGiamThi> generateGiamThiFull(
            int line,
            List<PhongThi> phongThiList,
            List<CanBo> canBoList) throws Exception {

        if (phongThiList == null || phongThiList.isEmpty()) {
            throw new IllegalArgumentException("Danh sach phong thi rong");
        }

        if (canBoList == null || canBoList.isEmpty()) {
            throw new IllegalArgumentException("Danh sach can bo rong");
        }

        List<PhongThi> sortedPhongThiList = new ArrayList<>(phongThiList);
        List<CanBo> sortedCanBoList = new ArrayList<>(canBoList);

        sortedPhongThiList.sort(Comparator.comparingLong(PhongThi::getId));
        sortedCanBoList.sort(Comparator.comparingLong(CanBo::getId));

        int soPhongThiFull = sortedPhongThiList.size();
        int soCanBoFull = sortedCanBoList.size();
        int S = getS(soCanBoFull, soPhongThiFull);

        if (S <= 0 || 2 * S > soCanBoFull) {
            throw new IllegalArgumentException("S khong hop le");
        }

        int Lmax = getLmax(soCanBoFull, soPhongThiFull);

        if (line >= Lmax) {
            throw new Exception("Khong the tao ca thi cho line=" + line);
        }

        List<PhanCongGiamThi> result = new ArrayList<>();

        for (int i = 0; i < soPhongThiFull; i++) {
            int a = (i + line) % S;
            int b = S + (i + 2 * line) % S;

            PhongThi phong = sortedPhongThiList.get(i);
            CanBo giamThi1 = sortedCanBoList.get(a);
            CanBo giamThi2 = sortedCanBoList.get(b);

            result.add(new PhanCongGiamThi(
                    phong.getPhongThi(),
                    giamThi1.getMaGV(),
                    giamThi2.getMaGV()));
        }

        return result;
    }

    public static List<CanBo> getCanBoConLai(
            List<PhanCongGiamThi> giamThiList,
            List<CanBo> canBoFullList) {
        List<CanBo> result = new ArrayList<>();
        Set<String> usedMaGV = new HashSet<>();

        if (giamThiList != null) {
            for (PhanCongGiamThi pc : giamThiList) {
                if (pc.getMaGiamThi1() != null) {
                    usedMaGV.add(pc.getMaGiamThi1());
                }

                if (pc.getMaGiamThi2() != null) {
                    usedMaGV.add(pc.getMaGiamThi2());
                }
            }
        }

        for (CanBo cb : canBoFullList) {
            if (!usedMaGV.contains(cb.getMaGV())) {
                result.add(cb);
            }
        }

        return result;
    }

    public static List<PhanCongGiamSat> generateGiamSat(
            List<PhongThi> phongThiList,
            List<CanBo> canBoConLai,
            int soCanBoGiamSat,
            GiamSatDAO giamSatDAO) {
        List<PhanCongGiamSat> result = new ArrayList<>();

        if (phongThiList == null || phongThiList.isEmpty()) {
            return result;
        }

        if (canBoConLai == null || canBoConLai.isEmpty()) {
            return result;
        }

        int soCanBoCanXep = Math.min(soCanBoGiamSat, canBoConLai.size());

        if (soCanBoCanXep <= 0) {
            return result;
        }

        int soKhoi = Math.min(phongThiList.size(), soCanBoCanXep);
        List<List<PhongThi>> khoiList = GenerateSchedule.chiaKhoiPhongThi(phongThiList, soKhoi);
        if (khoiList == null || khoiList.isEmpty()) {
            return result;
        }

        for (int i = 0; i < soCanBoCanXep; i++) {
            CanBo cb = canBoConLai.get(i);
            int startIndex = i % khoiList.size();

            List<String> phongTrongKhoi = toPhongList(khoiList.get(startIndex));

            if (giamSatDAO != null && cb != null && cb.getMaGV() != null
                    && !giamSatDAO.coTheGiamSatKhoi(cb.getMaGV(), phongTrongKhoi)) {
                boolean found = false;
                for (int j = 0; j < khoiList.size(); j++) {
                    List<String> altPhong = toPhongList(khoiList.get((startIndex + j) % khoiList.size()));
                    if (giamSatDAO.coTheGiamSatKhoi(cb.getMaGV(), altPhong)) {
                        phongTrongKhoi = altPhong;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                }
            }

            result.add(new PhanCongGiamSat(cb, phongTrongKhoi));
        }

        return result;
    }

    private static List<String> toPhongList(List<PhongThi> khoi) {
        List<String> phongTrongKhoi = new ArrayList<>();
        if (khoi == null) {
            return phongTrongKhoi;
        }

        for (PhongThi phong : khoi) {
            if (phong != null && phong.getPhongThi() != null) {
                phongTrongKhoi.add(phong.getPhongThi());
            }
        }
        return phongTrongKhoi;
    }

    public static List<List<PhongThi>> chiaKhoiPhongThi(
            List<PhongThi> phongThiList,
            int soKhoi) {
        List<List<PhongThi>> result = new ArrayList<>();

        if (phongThiList == null || phongThiList.isEmpty() || soKhoi <= 0) {
            return result;
        }

        List<PhongThi> sortedPhongThiList = new ArrayList<>(phongThiList);
        sortedPhongThiList.sort(Comparator.comparingLong(PhongThi::getId));

        int total = sortedPhongThiList.size();
        int base = total / soKhoi;
        int extra = total % soKhoi;
        int index = 0;

        for (int i = 0; i < soKhoi; i++) {
            int size = base;

            if (i >= soKhoi - extra) {
                size++;
            }

            List<PhongThi> khoi = new ArrayList<>();

            for (int j = 0; j < size && index < total; j++) {
                khoi.add(sortedPhongThiList.get(index++));
            }

            if (!khoi.isEmpty()) {
                result.add(khoi);
            }
        }

        return result;
    }

    public static int getLmax(int m, int n) {
        int S = getS(m, n);
        int Lmax = 0;
        if (S % 2 == 0) {
            if (m > 2 * n + 1) {
                S = S - 1;
                Lmax = S;
            } else {
                Lmax = S / 2;
            }
        } else {
            Lmax = S;
        }
        return Lmax;
    }

    public static int getS(int m, int n) {
        int S = m / 2;
        if (S % 2 == 0) {
            if (m > 2 * n + 1) {
                S = S - 1;
            }
        }
        return S;
    }
}