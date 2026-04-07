package com.dlvs.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.dlvs.model.HoaDon;
import com.dlvs.model.TaiKhoan;
import com.dlvs.model.VeBan;

/**
 * Lớp tiện ích quản lý kết nối SQLite và khởi tạo cơ sở dữ liệu.
 * CƠ CHẾ BỌC THÉP: Lưu dữ liệu trong thư mục User để tránh lỗi quyền ghi (Admin).
 */
public class DatabaseHelper {

    private static String getDbUrl() {
        // 1. CHẾ ĐỘ PORTABLE 100%: LƯU DATABASE NGAY CẠNH FILE .EXE
        String currentDir = System.getProperty("user.dir");
        File dbFolder = new File(currentDir, "database");
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }
        File dbFile = new File(dbFolder, "database_veso.db");
        // Sửa lỗi cực kỳ nguy hiểm: Đường dẫn C:\Users chứa \U, sqlite-jdbc >= 3.40 sẽ tự dịch \U thành unicode escape -> FAIL!
        // Giải pháp: Thay toàn bộ backslash \ bằng forward slash /
        return "jdbc:sqlite:" + dbFile.getAbsolutePath().replace('\\', '/');
    }

    /**
     * Trả về một Connection kết nối tới file SQLite.
     */
    public static Connection getConnection() throws SQLException {
        String url = getDbUrl();
        return DriverManager.getConnection(url);
    }

    /**
     * Khởi tạo các bảng trong CSDL (chạy khi ứng dụng khởi động).
     */
    public static void initializeDatabase() {
        // CƠ CHẾ PORTABLE: Ép SQLite giải nén DLL vào chung thư mục database (tránh bị chặn ở Temp)
        File dbFolder = new File(System.getProperty("user.dir"), "database");
        System.setProperty("org.sqlite.tmpdir", dbFolder.getAbsolutePath());
        
        String sqlKhachHang = """
                CREATE TABLE IF NOT EXISTS KhachHang (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    tenKhach    TEXT,
                    sdt         TEXT
                );
                """;

        String sqlKhoVe = """
                CREATE TABLE IF NOT EXISTS KhoVe (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    tenDai        TEXT,
                    ngayXo        TEXT,
                    soLuongNhap   INTEGER,
                    donGiaGoc     REAL
                );
                """;

        String sqlHoaDon = """
                CREATE TABLE IF NOT EXISTS HoaDon (
                    maHoaDon      TEXT PRIMARY KEY,
                    ngayTao       TEXT,
                    tenNhanVien   TEXT,
                    tenKhachHang  TEXT,
                    tongTienHang  REAL,
                    khachDua      REAL,
                    tienThoi      REAL
                );
                """;

        String sqlChiTietHoaDon = """
                CREATE TABLE IF NOT EXISTS ChiTietHoaDon (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    maHoaDon    TEXT,
                    tenHang     TEXT,
                    soLuong     INTEGER,
                    donGia      REAL,
                    chietKhau   REAL,
                    thanhTien   REAL,
                    FOREIGN KEY(maHoaDon) REFERENCES HoaDon(maHoaDon)
                );
                """;

        String sqlTaiKhoan = """
                CREATE TABLE IF NOT EXISTS TaiKhoan (
                    username    TEXT PRIMARY KEY,
                    password    TEXT,
                    fullName    TEXT,
                    role        TEXT
                );
                """;

        String sqlVeTra = """
                CREATE TABLE IF NOT EXISTS VeTra (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ngayTra TEXT,
                    soLuongVe INTEGER,
                    nhanVienThucHien TEXT
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlKhachHang);
            stmt.execute(sqlKhoVe);
            stmt.execute(sqlHoaDon);
            stmt.execute(sqlChiTietHoaDon);
            stmt.execute(sqlTaiKhoan);
            stmt.execute(sqlVeTra);

            // Kiểm tra bảng TaiKhoan có rỗng không
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM TaiKhoan");
            int count = rs.next() ? rs.getInt("count") : 0;
            if (count == 0) {
                stmt.execute("INSERT INTO TaiKhoan (username, password, fullName, role) VALUES ('admin', 'admin', 'Sếp', 'ADMIN')");
                stmt.execute("INSERT INTO TaiKhoan (username, password, fullName, role) VALUES ('nhanvien', '123456', 'Nhân Viên A', 'NHANVIEN')");
                System.out.println("[DB] Khởi tạo tài khoản mặc định thành công.");
            }

            System.out.println("[DB] Khởi tạo CSDL bọc thép tại: " + getDbUrl());

        } catch (Throwable e) {
            System.err.println("[DB] Lỗi khởi tạo: " + e.getMessage());
            e.printStackTrace();
            try {
                java.io.File desktop = new java.io.File(System.getProperty("user.home"), "Desktop");
                java.io.FileWriter fw = new java.io.FileWriter(new java.io.File(desktop, "Loi_LocPhatTai.txt"));
                fw.write("USER.DIR: " + System.getProperty("user.dir") + "\n");
                fw.write("URL: " + getDbUrl() + "\n");
                fw.write("LỖI SẬP LÕI:\n" + e.toString());
                for (StackTraceElement element : e.getStackTrace()) {
                    fw.write("\n" + element.toString());
                }
                
                // Print SQLite specific debug info
                fw.write("\n\nCaused by:\n");
                Throwable cause = e.getCause();
                while(cause != null) {
                    fw.write("\n" + cause.toString());
                    cause = cause.getCause();
                }
                fw.close();
            } catch (Exception ex) {}
        }
    }

    /**
     * Lấy tổng doanh thu của ngày hôm nay.
     */
    public static double getDoanhThuHomNay() {
        double doanhThu = 0.0;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String sql = "SELECT SUM(tongTienHang) AS total FROM HoaDon WHERE ngayTao LIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                doanhThu = rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính doanh thu: " + e.getMessage());
        }
        return doanhThu;
    }

    /**
     * Lấy danh sách hóa đơn của ngày hôm nay.
     */
    public static ObservableList<HoaDon> getDanhSachHoaDonHomNay() {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String sql = "SELECT * FROM HoaDon WHERE ngayTao LIKE ? ORDER BY ngayTao DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HoaDon hd = new HoaDon(
                    rs.getString("maHoaDon"),
                    rs.getString("ngayTao"),
                    rs.getString("tenNhanVien"),
                    rs.getString("tenKhachHang"),
                    rs.getDouble("tongTienHang"),
                    rs.getDouble("khachDua"),
                    rs.getDouble("tienThoi")
                );
                list.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi load danh sách hóa đơn: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy tổng doanh thu của ngày hôm nay cho một nhân viên cụ thể.
     */
    public static double getDoanhThuNhanVienHomNay(String tenNhanVien) {
        double doanhThu = 0.0;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String sql = "SELECT SUM(tongTienHang) AS total FROM HoaDon WHERE ngayTao LIKE ? AND tenNhanVien = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today + "%");
            pstmt.setString(2, tenNhanVien);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                doanhThu = rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính doanh thu cá nhân: " + e.getMessage());
        }
        return doanhThu;
    }

    /**
     * Lấy danh sách hóa đơn của ngày hôm nay do một nhân viên cụ thể lập.
     */
    public static ObservableList<HoaDon> getDanhSachHoaDonNhanVienHomNay(String tenNhanVien) {
        ObservableList<HoaDon> list = FXCollections.observableArrayList();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String sql = "SELECT * FROM HoaDon WHERE ngayTao LIKE ? AND tenNhanVien = ? ORDER BY ngayTao DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today + "%");
            pstmt.setString(2, tenNhanVien);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HoaDon hd = new HoaDon(
                    rs.getString("maHoaDon"),
                    rs.getString("ngayTao"),
                    rs.getString("tenNhanVien"),
                    rs.getString("tenKhachHang"),
                    rs.getDouble("tongTienHang"),
                    rs.getDouble("khachDua"),
                    rs.getDouble("tienThoi")
                );
                list.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi load danh sách hóa đơn cá nhân: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy chi tiết hóa đơn dựa vào mã hóa đơn
     */
    public static ObservableList<VeBan> getChiTietHoaDon(String maHoaDon) {
        ObservableList<VeBan> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE maHoaDon = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                VeBan ve = new VeBan(
                    rs.getString("tenHang"),
                    rs.getInt("soLuong"),
                    rs.getDouble("donGia"),
                    rs.getDouble("chietKhau"),
                    rs.getDouble("thanhTien")
                );
                list.add(ve);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy tất cả tài khoản nhân sự
     */
    public static ObservableList<TaiKhoan> getAllTaiKhoan() {
        ObservableList<TaiKhoan> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM TaiKhoan";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                TaiKhoan tk = new TaiKhoan(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("fullName"),
                    rs.getString("role")
                );
                list.add(tk);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách tài khoản: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm tài khoản mới
     */
    public static boolean insertTaiKhoan(TaiKhoan tk) {
        String sql = "INSERT INTO TaiKhoan (username, password, fullName, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tk.getUsername());
            pstmt.setString(2, tk.getPassword());
            pstmt.setString(3, tk.getFullName());
            pstmt.setString(4, tk.getRole());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insert tài khoản: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật tài khoản
     */
    public static boolean updateTaiKhoan(TaiKhoan tk, String originalUsername) {
        String sql = "UPDATE TaiKhoan SET username = ?, password = ?, fullName = ?, role = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tk.getUsername());
            pstmt.setString(2, tk.getPassword());
            pstmt.setString(3, tk.getFullName());
            pstmt.setString(4, tk.getRole());
            pstmt.setString(5, originalUsername);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi update tài khoản: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa tài khoản
     */
    public static boolean deleteTaiKhoan(String username) {
        String sql = "DELETE FROM TaiKhoan WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi delete tài khoản: " + e.getMessage());
            return false;
        }
    }

    /**
     * Thêm vé ế (trả vé) vào cơ sở dữ liệu.
     */
    public static boolean insertVeTra(int soLuongVe, String nhanVienThucHien) {
        String ngayTra = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String sql = "INSERT INTO VeTra (ngayTra, soLuongVe, nhanVienThucHien) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ngayTra);
            pstmt.setInt(2, soLuongVe);
            pstmt.setString(3, nhanVienThucHien);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insert vé trả: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy tổng số vé đã trả của một nhân viên trong ngày hôm nay.
     */
    public static int getTongSoVeTraHomNay(String tenNhanVien) {
        int tongVe = 0;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String sql = "SELECT SUM(soLuongVe) AS total FROM VeTra WHERE ngayTra = ? AND nhanVienThucHien = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            pstmt.setString(2, tenNhanVien);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                tongVe = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính số vé trả cá nhân: " + e.getMessage());
        }
        return tongVe;
    }

    /**
     * Lấy tổng số vé đã trả của TOÀN HỆ THỐNG trong ngày hôm nay.
     */
    public static int getTongVeTraToanHeThongHomNay() {
        int tongVe = 0;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String sql = "SELECT SUM(soLuongVe) AS total FROM VeTra WHERE ngayTra = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                tongVe = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính tổng số vé trả hệ thống: " + e.getMessage());
        }
        return tongVe;
    }
}
