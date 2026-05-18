package ckthltm.dal;

import ckthltm.models.CanBo;
import java.sql.*;
import java.util.*;

public class CanBoDAO extends BaseDAO {
    public List<CanBo> getAll() {
        List<CanBo> list = new ArrayList<>();
        String sql = "SELECT * FROM can_bo ORDER BY id";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                CanBo cb = new CanBo(
                        rs.getLong("id"),
                        rs.getString("ma_gv"),
                        rs.getString("ho_ten"),
                        rs.getString("ngay_sinh"),
                        rs.getString("don_vi"));
                list.add(cb);
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

    public CanBo getById(int id) {
        CanBo cb = null;
        String sql = "SELECT * FROM can_bo WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                cb = new CanBo(
                        rs.getLong("id"),
                        rs.getString("ma_gv"),
                        rs.getString("ho_ten"),
                        rs.getString("ngay_sinh"),
                        rs.getString("don_vi"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return cb;
    }

    public boolean insert(CanBo cb) {
        String sql = "INSERT INTO can_bo (ma_gv, ho_ten, ngay_sinh, don_vi) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cb.getMaGV());
            pstmt.setString(2, cb.getHoTen());
            pstmt.setString(3, cb.getNgaySinh());
            pstmt.setString(4, cb.getDonViCongTac());
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
            stmt.executeUpdate("DELETE FROM can_bo");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
