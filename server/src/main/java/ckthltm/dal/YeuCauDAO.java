package ckthltm.dal;

import ckthltm.models.YeuCau;

import java.sql.*;
import java.util.*;

public class YeuCauDAO extends BaseDAO {
    public List<YeuCau> getAll() {
        List<YeuCau> list = new ArrayList<>();
        String sql = "SELECT * FROM yeu_cau ORDER BY line, id";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                YeuCau yc = new YeuCau(
                        rs.getInt("id"),
                        rs.getInt("so_can_bo"),
                        rs.getInt("so_phong_thi"),
                    rs.getInt("line"));
                list.add(yc);
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

    public YeuCau getById(int id) {
        YeuCau yc = null;
        String sql = "SELECT * FROM yeu_cau WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                yc = new YeuCau(
                        rs.getInt("id"),
                        rs.getInt("so_can_bo"),
                        rs.getInt("so_phong_thi"),
                    rs.getInt("line"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return yc;
    }

    public boolean insert(YeuCau yc) {
        String sql = "INSERT INTO yeu_cau (so_can_bo, so_phong_thi, line) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, yc.getSoCanBo());
            pstmt.setInt(2, yc.getSoPhongThi());
            pstmt.setInt(3, yc.getLine());
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
            stmt.executeUpdate("DELETE FROM yeu_cau");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
