package com.dlvs.dao;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.dlvs.model.HoaDon;
import com.dlvs.model.TaiKhoan;
import com.dlvs.model.VeBan;

public class DatabaseHelper {
    private static String getDbUrl() {
        String currentDir = System.getProperty("user.dir");
        File dbFolder = new File(currentDir, "database");
        if (!dbFolder.exists()) dbFolder.mkdirs();
        File dbFile = new File(dbFolder, "database_veso.db");
        return "jdbc:sqlite:" + dbFile.getAbsolutePath().replace('\\', '/');
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl());
    }

    public static void initializeDatabase() {
        File dbFolder = new File(System.getProperty("user.dir"), "database");
        System.setProperty("org.sqlite.tmpdir", dbFolder.getAbsolutePath());
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS KhachHang (id INTEGER PRIMARY KEY AUTOINCREMENT, tenKhach TEXT, sdt TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS HoaDon (maHoaDon TEXT PRIMARY KEY, ngayTao TEXT, tenNhanVien TEXT, tenKhachHang TEXT, tongTienHang REAL, khachDua REAL, tienThoi REAL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS ChiTietHoaDon (id INTEGER PRIMARY KEY AUTOINCREMENT, maHoaDon TEXT, tenHang TEXT, soLuong INTEGER, donGia REAL, chietKhau REAL, thanhTien REAL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS TaiKhoan (username TEXT PRIMARY KEY, password TEXT, fullName TEXT, role TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS VeTra (id INTEGER PRIMARY KEY AUTOINCREMENT, ngayTra TEXT, soLuongVe INTEGER, nhanVienThucHien TEXT);");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Các hàm thống kê
    public static double getDoanhThuHomNay() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT SUM(tongTienHang) FROM HoaDon WHERE ngayTao LIKE ?")) {
            pstmt.setString(1, today + "%");
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static ObservableList<HoaDon> getDanhSachHoaDonHomNay() {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM HoaDon WHERE ngayTao LIKE ? ORDER BY ngayTao DESC")) {
            pstmt.setString(1, today + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(new HoaDon(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getDouble(6), rs.getDouble(7)));
        } catch (Exception e) { }
        return list;
    }

    public static ObservableList<VeBan> getChiTietHoaDon(String ma) {
        ObservableList<VeBan> list = FXCollections.observableArrayList();
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
            pstmt.setString(1, ma);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(new VeBan(rs.getString(3), rs.getInt(4), rs.getDouble(5), rs.getDouble(6), rs.getDouble(7)));
        } catch (Exception e) { }
        return list;
    }

    // Quản lý tài khoản
    public static ObservableList<TaiKhoan> getAllTaiKhoan() {
        ObservableList<TaiKhoan> list = FXCollections.observableArrayList();
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM TaiKhoan")) {
            while (rs.next()) list.add(new TaiKhoan(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
        } catch (Exception e) { }
        return list;
    }

    public static boolean insertTaiKhoan(TaiKhoan t) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO TaiKhoan VALUES(?,?,?,?)")) {
            ps.setString(1, t.getUsername()); ps.setString(2, t.getPassword()); ps.setString(3, t.getFullName()); ps.setString(4, t.getRole());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public static boolean updateTaiKhoan(TaiKhoan t, String old) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE TaiKhoan SET username=?, password=?, fullName=?, role=? WHERE username=?")) {
            ps.setString(1, t.getUsername()); ps.setString(2, t.getPassword()); ps.setString(3, t.getFullName()); ps.setString(4, t.getRole()); ps.setString(5, old);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public static boolean deleteTaiKhoan(String u) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM TaiKhoan WHERE username=?")) {
            ps.setString(1, u); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    // TÍNH NĂNG VÉ TRẢ
    public static boolean insertVeTra(int sl, String nv) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO VeTra (ngayTra, soLuongVe, nhanVienThucHien) VALUES (?,?,?)")) {
            ps.setString(1, today); ps.setInt(2, sl); ps.setString(3, nv);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public static int getTongVeTraHomNay() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT SUM(soLuongVe) FROM VeTra WHERE ngayTra=?")) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static int getTongVeTraNhanVienHomNay(String nv) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT SUM(soLuongVe) FROM VeTra WHERE ngayTra=? AND nhanVienThucHien=?")) {
            ps.setString(1, today); ps.setString(2, nv);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }
    
    public static double getDoanhThuNhanVienHomNay(String nv) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT SUM(tongTienHang) FROM HoaDon WHERE ngayTao LIKE ? AND tenNhanVien=?")) {
            ps.setString(1, today + "%"); ps.setString(2, nv);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static ObservableList<HoaDon> getDanhSachHoaDonNhanVienHomNay(String nv) {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM HoaDon WHERE ngayTao LIKE ? AND tenNhanVien=?")) {
            ps.setString(1, today + "%"); ps.setString(2, nv);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new HoaDon(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getDouble(6), rs.getDouble(7)));
        } catch (Exception e) { }
        return list;
    }
}