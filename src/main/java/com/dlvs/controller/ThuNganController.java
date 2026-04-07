package com.dlvs.controller;

import com.dlvs.model.VeBan;
import com.dlvs.model.UserSession;
import com.dlvs.util.NumberToWords;
import com.dlvs.dao.DatabaseHelper;
import com.dlvs.model.HoaDon;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ThuNganController {

    @FXML
    private TextField tenDaiField;
    @FXML
    private TextField soLuongField;
    @FXML
    private TextField donGiaField;
    @FXML
    private TextField chietKhauField;
    @FXML
    private Button btnThemVe;

    @FXML
    private TableView<VeBan> bangVe;
    @FXML
    private TableColumn<VeBan, String> colTenHang;
    @FXML
    private TableColumn<VeBan, Integer> colSoLuong;
    @FXML
    private TableColumn<VeBan, Double> colDonGia;
    @FXML
    private TableColumn<VeBan, Double> colChietKhau;
    @FXML
    private TableColumn<VeBan, Double> colThanhTien;

    @FXML
    private TextField tenKhachField;
    @FXML
    private Label lblTongTien;
    @FXML
    private TextField tienKhachDuaField;
    @FXML
    private Label lblTienThoi;
    @FXML
    private TextArea txtPreviewBienLai;
    @FXML
    private Button btnThanhToan;

    @FXML
    private Label lblTenNhanVien;
    @FXML
    private Label lblDoanhSoCaNhan;
    @FXML
    private Label lblTongVeTra;
    @FXML
    private Label lblTongSoHoaDon;

    @FXML
    private Button btnTraVeE;

    @FXML
    private TableView<HoaDon> bangHoaDonCaNhan;
    @FXML
    private TableColumn<HoaDon, String> colMaHoaDonCaNhan;
    @FXML
    private TableColumn<HoaDon, String> colNgayTaoCaNhan;
    @FXML
    private TableColumn<HoaDon, String> colKhachHangCaNhan;
    @FXML
    private TableColumn<HoaDon, Double> colTongTienHoaDonCaNhan;

    private ObservableList<VeBan> danhSachVe;
    private double tongTienHang = 0.0;
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        // Khởi tạo danh sách cho TableView Giỏ Hàng
        danhSachVe = FXCollections.observableArrayList();
        bangVe.setItems(danhSachVe);

        // Cấu hình các cột Giỏ Hàng
        colTenHang.setCellValueFactory(new PropertyValueFactory<>("tenHang"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colChietKhau.setCellValueFactory(new PropertyValueFactory<>("chietKhau"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));

        // Lắng nghe sự kiện gõ vào ô Tiền khách đưa
        tienKhachDuaField.textProperty().addListener((observable, oldValue, newValue) -> {
            tinhTienThoi();
        });

        // Initialize Tab 2 (Lịch sử cá nhân)
        if (UserSession.getInstance() != null) {
            lblTenNhanVien.setText("Xin chào, " + UserSession.getInstance().getFullName() + "!");
        }
        
        // Cập nhật số vé trả ngay khi mở form
        updateTongVeTra();

        colMaHoaDonCaNhan.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        colNgayTaoCaNhan.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colKhachHangCaNhan.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
        colTongTienHoaDonCaNhan.setCellValueFactory(new PropertyValueFactory<>("tongTienHang"));

        colTongTienHoaDonCaNhan.setCellFactory(tc -> new javafx.scene.control.TableCell<HoaDon, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(price) + " đ");
                }
            }
        });

        handleRefreshCaNhan();
    }

    @FXML
    private void handleThemVe(ActionEvent event) {
        try {
            String tenHang = tenDaiField.getText();
            int soLuong = Integer.parseInt(soLuongField.getText().isEmpty() ? "0" : soLuongField.getText());
            double donGia = Double.parseDouble(donGiaField.getText().isEmpty() ? "0" : donGiaField.getText());
            double chietKhau = Double.parseDouble(chietKhauField.getText().isEmpty() ? "0" : chietKhauField.getText());

            double thanhTien = (soLuong * donGia) - chietKhau;
            if (thanhTien < 0) thanhTien = 0;

            VeBan ve = new VeBan(tenHang, soLuong, donGia, chietKhau, thanhTien);
            danhSachVe.add(ve);

            // Cập nhật tổng tiền
            cargTongTien();

            // Clear input
            tenDaiField.clear();
            soLuongField.clear();
            donGiaField.clear();
            chietKhauField.clear();

        } catch (NumberFormatException e) {
            // Hiển thị lỗi hoặc bỏ qua trong lúc demo
            System.err.println("Lỗi nhập số liệu: " + e.getMessage());
        }
    }

    private void cargTongTien() {
        tongTienHang = 0;
        for (VeBan ve : danhSachVe) {
            tongTienHang += ve.getThanhTien();
        }
        lblTongTien.setText(String.format("%.2f", tongTienHang));
        tinhTienThoi(); // Tính lại tiền thối
    }

    private void tinhTienThoi() {
        try {
            String tienDuaStr = tienKhachDuaField.getText();
            if (tienDuaStr == null || tienDuaStr.trim().isEmpty()) {
                lblTienThoi.setText("0.00");
                return;
            }
            double tienDua = Double.parseDouble(tienDuaStr.trim());
            double tienThoi = tienDua - tongTienHang;
            
            lblTienThoi.setText(String.format("%.2f", tienThoi));
        } catch (NumberFormatException e) {
            // Nếu người dùng nhập sai (cố tình nhập chữ), cứ để label báo lỗi hoặc 0
            lblTienThoi.setText("Lỗi định dạng");
        }
    }

    private String generateMaHoaDon() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "BL-" + now.format(formatter);
    }

    @FXML
    private void handleThanhToan(ActionEvent event) {
        if (danhSachVe.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Giỏ hàng đang trống! Vui lòng thêm vé trước khi thanh toán.");
            alert.showAndWait();
            return;
        }

        String maHoaDon = generateMaHoaDon();
        String ngayTao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss"));
        String tenNhanVien = UserSession.getInstance() != null ? UserSession.getInstance().getFullName() : "Nhân viên";
        String tenKhachHang = tenKhachField.getText().isEmpty() ? "Khách lẻ" : tenKhachField.getText();
        
        double tienDua = 0.0;
        double tienThoi = 0.0;
        try {
            tienDua = Double.parseDouble(tienKhachDuaField.getText().isEmpty() ? "0" : tienKhachDuaField.getText());
            tienThoi = tienDua - tongTienHang;
        } catch (NumberFormatException e) {
            tienDua = 0.0;
            tienThoi = 0.0;
        }

        String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, ngayTao, tenNhanVien, tenKhachHang, tongTienHang, khachDua, tienThoi) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlChiTiet = "INSERT INTO ChiTietHoaDon (maHoaDon, tenHang, soLuong, donGia, chietKhau, thanhTien) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmtHoaDon = conn.prepareStatement(sqlHoaDon);
             PreparedStatement pstmtChiTiet = conn.prepareStatement(sqlChiTiet)) {
             
            // Lưu Hóa Đơn
            pstmtHoaDon.setString(1, maHoaDon);
            pstmtHoaDon.setString(2, ngayTao);
            pstmtHoaDon.setString(3, tenNhanVien);
            pstmtHoaDon.setString(4, tenKhachHang);
            pstmtHoaDon.setDouble(5, tongTienHang);
            pstmtHoaDon.setDouble(6, tienDua);
            pstmtHoaDon.setDouble(7, tienThoi);
            pstmtHoaDon.executeUpdate();

            // Lưu Chi Tiết
            for (VeBan ve : danhSachVe) {
                pstmtChiTiet.setString(1, maHoaDon);
                pstmtChiTiet.setString(2, ve.getTenHang());
                pstmtChiTiet.setInt(3, ve.getSoLuong());
                pstmtChiTiet.setDouble(4, ve.getDonGia());
                pstmtChiTiet.setDouble(5, ve.getChietKhau());
                pstmtChiTiet.setDouble(6, ve.getThanhTien());
                pstmtChiTiet.addBatch();
            }
            pstmtChiTiet.executeBatch();

            // Sinh Preview Biên lai
            String preview = generateReceiptText(maHoaDon, tenNhanVien, tenKhachHang, ngayTao, tienDua, tienThoi);
            txtPreviewBienLai.setText(preview);

            // Báo thành công
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Thanh toán thành công Hóa Đơn: " + maHoaDon);
            alert.showAndWait();

            resetGiaoDien();
            handleRefreshCaNhan();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Lỗi Database");
            alert.setHeaderText(null);
            alert.setContentText("Không thể lưu hóa đơn: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private String generateReceiptText(String maHD, String nhanVien, String khach, String thoiGian, double tienDua, double tienThoi) {
        StringBuilder sb = new StringBuilder();
        sb.append("\tKÍNH CHÚC QUÝ KHÁCH MAY MẮN\n");
        sb.append("ĐỔI VÉ TRÚNG ĐẶC BIỆT 0785423259 - 0976814652 PHIẾU CÓ\n");
        sb.append("GIÁ TRỊ 3 NGÀY KỂ TỪ NGÀY SỔ XỐ\n");
        sb.append("ĐẠI LÝ VÉ SỐ LỘC PHÁT TÀI\n");
        sb.append("ĐT: 0785423259 - 0976814652\n");
        sb.append("HÓA ĐƠN BÁN LẺ\n");
        sb.append("Khách hàng : ").append(khach).append("\n");
        sb.append("Ngày :\t\t").append(thoiGian).append("\n");
        sb.append("Nhân viên: \t").append(nhanVien).append("\n");
        sb.append("Số hóa đơn : \t").append(maHD).append("\n\n");
        
        sb.append(String.format("%-15s %-10s %-10s %-8s %s\n", "Tên Hàng", "SỐ LƯỢNG", "ĐƠN GIÁ", "KM", "TIỀN"));
        
        int tongSoLuong = 0;
        for (VeBan ve : danhSachVe) {
            String ten = ve.getTenHang();
            if (ten.length() > 14) ten = ten.substring(0, 14);
            sb.append(String.format("%-15s %-10d %-10.0f %-8.0f %.0f\n", ten, ve.getSoLuong(), ve.getDonGia(), ve.getChietKhau(), ve.getThanhTien()));
            tongSoLuong += ve.getSoLuong();
        }
        
        sb.append("Tổng cộng số lượng: ").append(tongSoLuong).append("\n");
        sb.append(String.format("Tổng tiền hàng: %.0f VNĐ\n", tongTienHang));
        sb.append("(Bằng chữ: ").append(NumberToWords.convert(tongTienHang)).append("./.)\n");
        sb.append(String.format("Khách hàng trả - nhận tiền từ khách : %.0f\n", tienDua));
        sb.append(String.format("Tiền thối : %.0f\n\n", tienThoi));
        sb.append("cảm ơn quý khách");
        return sb.toString();
    }

    private void resetGiaoDien() {
        danhSachVe.clear();
        tongTienHang = 0.0;
        lblTongTien.setText("0.0");
        lblTienThoi.setText("0.0");
        tenKhachField.clear();
        tienKhachDuaField.clear();
        // Không clear Preview để nhân viên còn thấy biên lai mẫu vừa in
    }

    @FXML
    void handleRefreshCaNhan() {
        if (UserSession.getInstance() == null) return;
        
        String tenNhanVien = UserSession.getInstance().getFullName();
        
        ObservableList<HoaDon> danhSach = DatabaseHelper.getDanhSachHoaDonNhanVienHomNay(tenNhanVien);
        bangHoaDonCaNhan.setItems(danhSach);
        
        double doanhThu = DatabaseHelper.getDoanhThuNhanVienHomNay(tenNhanVien);
        lblDoanhSoCaNhan.setText("Doanh số bạn đã bán hôm nay: " + currencyFormat.format(doanhThu) + " VNĐ");
        lblTongSoHoaDon.setText("Tổng số hóa đơn: " + danhSach.size());
        
        // Cập nhật nhãn vé trả khi refresh
        updateTongVeTra();
    }

    @FXML
    public void handleTraVeE(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Trả Vé Ế");
        dialog.setHeaderText("Quản lý trả vé ế. Xin lưu ý hệ thống sẽ lưu dấu vé trả này theo Tên của bạn!");
        dialog.setContentText("Nhập chính xác số lượng vé ế cần trả lại:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String input = result.get().trim();
            if (input.isEmpty()) return;
            try {
                int soLuong = Integer.parseInt(input);
                if (soLuong <= 0) {
                    throw new NumberFormatException();
                }
                String nhanVienId = UserSession.getInstance() != null ? UserSession.getInstance().getUsername() : "UNKNOWN";
                boolean success = DatabaseHelper.insertVeTra(soLuong, nhanVienId);
                if (success) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Đã ghi nhận kho xử lý trả " + soLuong + " vé ế thành công!");
                    alert.showAndWait();
                    updateTongVeTra();
                } else {
                    Alert alert = new Alert(AlertType.ERROR, "Lỗi khi lưu dữ liệu vé trả.");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(AlertType.ERROR, "Số lượng vé không hợp lệ! Vui lòng nhập số đếm nguyên dương.");
                alert.showAndWait();
            }
        }
    }

    private void updateTongVeTra() {
        if (UserSession.getInstance() != null && lblTongVeTra != null) {
            int tongVeTra = DatabaseHelper.getTongSoVeTraHomNay(UserSession.getInstance().getUsername());
            lblTongVeTra.setText("Tổng số vé trả hôm nay: " + tongVeTra);
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            UserSession.cleanUserSession();
            Parent root = FXMLLoader.load(getClass().getResource("/com/dlvs/fxml/DangNhapView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ĐẠI LÝ VÉ SỐ LỘC PHÁT TÀI");
            stage.setScene(new Scene(root, 400, 400));
            stage.show();
        } catch (Exception e) {
            System.err.println("Lỗi khi đăng xuất: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
