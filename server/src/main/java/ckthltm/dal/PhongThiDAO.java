package ckthltm.dal;

import ckthltm.models.PhongThi;
import java.sql.*;
import java.util.*;

public class PhongThiDAO extends BaseDAO {
    public List<PhongThi> getAll() {
        List<PhongThi> list = new ArrayList<>();
        String sql = "SELECT * FROM phong_thi ORDER BY id";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                PhongThi pt = new PhongThi(
                        rs.getLong("id"),
                        rs.getString("phong_thi"),
                        rs.getString("dia_diem"));
                list.add(pt);
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

    public PhongThi getById(int id) {
        PhongThi pt = null;
        String sql = "SELECT * FROM phong_thi WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                pt = new PhongThi(
                        rs.getLong("id"),
                        rs.getString("phong_thi"),
                        rs.getString("dia_diem"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return pt;
    }

    public boolean insert(PhongThi pt) {
        String sql = "INSERT INTO phong_thi (phong_thi, dia_diem) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pt.getPhongThi());
            pstmt.setString(2, pt.getDiaDiem());
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
            stmt.executeUpdate("DELETE FROM phong_thi");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
