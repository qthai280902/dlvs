package com.dlvs.controller;

import com.dlvs.model.*;
import com.dlvs.dao.DatabaseHelper;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ThuNganController {
    @FXML private TextField tenDaiField, soLuongField, donGiaField, chietKhauField, tenKhachField, tienKhachDuaField;
    @FXML private Label lblTongTien, lblTienThoi, lblTenNhanVien, lblDoanhSoCaNhan, lblTongSoHoaDon, lblTongVeTra;
    @FXML private TableView<VeBan> bangVe;
    @FXML private TableColumn<VeBan, String> colTenHang;
    @FXML private TableColumn<VeBan, Integer> colSoLuong;
    @FXML private TableColumn<VeBan, Double> colDonGia, colChietKhau, colThanhTien;
    @FXML private TableView<HoaDon> bangHoaDonCaNhan;
    @FXML private TableColumn<HoaDon, String> colMaHoaDonCaNhan, colNgayTaoCaNhan, colKhachHangCaNhan;
    @FXML private TableColumn<HoaDon, Double> colTongTienHoaDonCaNhan;

    private ObservableList<VeBan> items = FXCollections.observableArrayList();
    private double tong = 0;
    private DecimalFormat df = new DecimalFormat("#,###");

    @FXML public void initialize() {
        bangVe.setItems(items);
        colTenHang.setCellValueFactory(new PropertyValueFactory<>("tenHang"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colChietKhau.setCellValueFactory(new PropertyValueFactory<>("chietKhau"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colMaHoaDonCaNhan.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        colNgayTaoCaNhan.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colKhachHangCaNhan.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
        colTongTienHoaDonCaNhan.setCellValueFactory(new PropertyValueFactory<>("tongTienHang"));
        
        tienKhachDuaField.textProperty().addListener((o, old, nv) -> {
            try { lblTienThoi.setText(String.format("%.2f", Double.parseDouble(nv) - tong)); } catch(Exception e) {}
        });
        if (UserSession.getInstance() != null) lblTenNhanVien.setText("Chào, " + UserSession.getInstance().getFullName());
        handleRefreshCaNhan();
    }

    @FXML void handleThemVe(ActionEvent e) {
        try {
            double dg = Double.parseDouble(donGiaField.getText());
            int sl = Integer.parseInt(soLuongField.getText());
            double ck = chietKhauField.getText().isEmpty() ? 0 : Double.parseDouble(chietKhauField.getText());
            items.add(new VeBan(tenDaiField.getText(), sl, dg, ck, (sl*dg)-ck));
            tong = items.stream().mapToDouble(VeBan::getThanhTien).sum();
            lblTongTien.setText(String.format("%.2f", tong));
        } catch(Exception ex) {}
    }

    @FXML void handleThanhToan(ActionEvent e) {
        if (items.isEmpty()) return;
        String ma = "BL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO HoaDon VALUES(?,?,?,?,?,?,?)");
            ps.setString(1, ma); ps.setString(2, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss")));
            ps.setString(3, UserSession.getInstance().getFullName()); ps.setString(4, tenKhachField.getText());
            ps.setDouble(5, tong); ps.setDouble(6, Double.parseDouble(tienKhachDuaField.getText())); ps.setDouble(7, Double.parseDouble(lblTienThoi.getText()));
            ps.executeUpdate();
            items.clear(); tong = 0; lblTongTien.setText("0"); handleRefreshCaNhan();
        } catch(Exception ex) { ex.printStackTrace(); }
    }

    @FXML void handleRefreshCaNhan(ActionEvent e) { handleRefreshCaNhan(); }

    private void handleRefreshCaNhan() {
        String name = UserSession.getInstance().getFullName();
        bangHoaDonCaNhan.setItems(DatabaseHelper.getDanhSachHoaDonNhanVienHomNay(name));
        lblDoanhSoCaNhan.setText("Doanh số: " + df.format(DatabaseHelper.getDoanhThuNhanVienHomNay(name)) + " VNĐ");
        if (lblTongVeTra != null) {
            lblTongVeTra.setText("Vé trả hôm nay: " + DatabaseHelper.getTongVeTraNhanVienHomNay(UserSession.getInstance().getUsername()));
        }
    }

    @FXML void handleTraVeE(ActionEvent e) {
        TextInputDialog d = new TextInputDialog("0");
        d.setTitle("Trả vé ế");
        d.setHeaderText("Nhập số lượng vé trả:");
        d.showAndWait().ifPresent(v -> {
            try { 
                DatabaseHelper.insertVeTra(Integer.parseInt(v), UserSession.getInstance().getUsername()); 
                handleRefreshCaNhan(); 
            } catch(Exception ex) {}
        });
    }

    @FXML void handleLogout(ActionEvent e) {
        try {
            UserSession.cleanUserSession();
            Parent r = FXMLLoader.load(getClass().getResource("/com/dlvs/fxml/DangNhapView.fxml"));
            ((Stage)((Node)e.getSource()).getScene().getWindow()).setScene(new Scene(r, 400, 400));
        } catch(Exception ex) {}
    }
}