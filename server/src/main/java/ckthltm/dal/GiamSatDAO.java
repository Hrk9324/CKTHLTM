package ckthltm.dal;

import ckthltm.models.GiamSat;
import ckthltm.models.result.PhanCongGiamSat;

import java.sql.*;
import java.util.*;

public class GiamSatDAO extends BaseDAO {
    public List<GiamSat> getAll() {
        List<GiamSat> list = new ArrayList<>();
        String sql = "SELECT * FROM giam_sat";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                GiamSat gs = new GiamSat(
                        rs.getLong("id"),
                        rs.getString("ma_gv"),
                        rs.getString("phong_thi"));
                list.add(gs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return list;
    }

    public GiamSat getById(int id) {
        GiamSat gs = null;
        String sql = "SELECT * FROM giam_sat WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                gs = new GiamSat(
                        rs.getLong("id"),
                        rs.getString("ma_gv"),
                        rs.getString("phong_thi"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return gs;
    }

    public boolean insert(GiamSat gs) {
        String sql = "INSERT INTO giam_sat (ma_gv, phong_thi) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, gs.getMaGV());
            pstmt.setString(2, gs.getPhongThi());
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return false;
    }

    public void deleteAll() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM giam_sat");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertAllPhanCongGiamSat(List<PhanCongGiamSat> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (PhanCongGiamSat pc : list) {
            if (pc.getCanBo() == null) {
                continue;
            }

            for (String phong : pc.getPhongThiList()) {
                GiamSat gs = new GiamSat(
                        0,
                        pc.getCanBo().getMaGV(),
                        phong);

                insert(gs);
            }
        }
    }

    public boolean daTungGiamSatPhong(String maGV, String phongThi) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM giam_sat
                WHERE ma_gv = ?
                  AND phong_thi = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maGV);
            ps.setString(2, phongThi);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean coTheGiamSatKhoi(String maGV, List<String> phongThiList) {
        if (maGV == null || maGV.trim().isEmpty()) {
            return false;
        }

        if (phongThiList == null || phongThiList.isEmpty()) {
            return false;
        }

        for (String phongThi : phongThiList) {
            if (daTungGiamSatPhong(maGV, phongThi)) {
                return false;
            }
        }

        return true;
    }
}
