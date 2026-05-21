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

        final String sql = "INSERT INTO giam_sat (ma_gv, phong_thi) VALUES (?, ?)";

        int rowsToInsert = 0;

        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                throw new SQLException("Cannot obtain DB connection");
            }

            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (PhanCongGiamSat pc : list) {
                    if (pc == null || pc.getCanBo() == null) {
                        continue;
                    }

                    List<String> phongList = pc.getPhongThiList();
                    if (phongList == null || phongList.isEmpty()) {
                        continue;
                    }

                    String maGV = pc.getCanBo().getMaGV();
                    for (String phong : phongList) {
                        ps.setString(1, maGV);
                        ps.setString(2, phong);
                        ps.addBatch();
                        rowsToInsert++;
                    }
                }

                if (rowsToInsert == 0) {
                    conn.commit();
                    return;
                }

                ps.executeBatch();
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback error: " + rollbackEx.getMessage());
                }
            }

            System.err.println("Failed to batch insert giam_sat: " + e.getMessage());
            throw new RuntimeException("Failed to batch insert giam_sat", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Failed to restore auto-commit: " + e.getMessage());
                }
                closeConnection(conn);
            }
        }
    }

    // public boolean daTungGiamSatPhong(String maGV, String phongThi) {
    //     String sql = """
    //             SELECT 1
    //             FROM giam_sat
    //             WHERE ma_gv = ?
    //               AND phong_thi = ?
    //             LIMIT 1
    //             """;

    //     try (Connection conn = getConnection();
    //             PreparedStatement ps = conn.prepareStatement(sql)) {

    //         ps.setString(1, maGV);
    //         ps.setString(2, phongThi);

    //         try (ResultSet rs = ps.executeQuery()) {
    //             return rs.next();
    //         }

    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }

    //     return false;
    // }

    // public boolean coTheGiamSatKhoi(String maGV, List<String> phongThiList) {
    //     if (maGV == null || maGV.trim().isEmpty()) {
    //         return false;
    //     }

    //     if (phongThiList == null || phongThiList.isEmpty()) {
    //         return false;
    //     }

    //     String placeholders = String.join(",", Collections.nCopies(phongThiList.size(), "?"));
    //     String sql = "SELECT 1 FROM giam_sat WHERE ma_gv = ? AND phong_thi IN (" + placeholders + ") LIMIT 1";

    //     try (Connection conn = getConnection()) {
    //         if (conn == null) {
    //             System.err.println("Cannot obtain DB connection");
    //             return false;
    //         }

    //         try (PreparedStatement ps = conn.prepareStatement(sql)) {
    //             ps.setString(1, maGV);
    //             for (int i = 0; i < phongThiList.size(); i++) {
    //                 ps.setString(i + 2, phongThiList.get(i));
    //             }

    //             try (ResultSet rs = ps.executeQuery()) {
    //                 boolean found = rs.next();
    //                 return !found;
    //             }
    //         }
    //     } catch (SQLException e) {
    //         System.err.println("SQL error in coTheGiamSatKhoi: " + e.getMessage());
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
}
