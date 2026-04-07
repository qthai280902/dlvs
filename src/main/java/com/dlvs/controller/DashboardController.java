package com.dlvs.controller;

import com.dlvs.dao.DatabaseHelper;
import com.dlvs.model.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.*;
import java.awt.Desktop;

public class DashboardController {
    @FXML private Label lblDoanhThu, lblTongVeTraHomNay;
    @FXML private TableView<HoaDon> bangHoaDon;
    @FXML private TableColumn<HoaDon, String> colMaHoaDon, colNgayTao, colTenNhanVien, colTenKhachHang;
    @FXML private TableColumn<HoaDon, Double> colTongTien;
    @FXML private TableView<VeBan> tableChiTietHoaDon;
    @FXML private TableColumn<VeBan, String> colTenHang;
    @FXML private TableColumn<VeBan, Integer> colSoLuong;
    @FXML private TableColumn<VeBan, Double> colDonGia, colThanhTien;
    @FXML private TextField txtUsername, txtFullName;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private TableView<TaiKhoan> tableNhanVien;
    @FXML private TableColumn<TaiKhoan, String> colUsernameNV, colFullNameNV, colRoleNV;

    private String selectedUser = null;
    private DecimalFormat df = new DecimalFormat("#,###");

    @FXML public void initialize() {
        colMaHoaDon.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTenNhanVien.setCellValueFactory(new PropertyValueFactory<>("tenNhanVien"));
        colTenKhachHang.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTienHang"));
        colTenHang.setCellValueFactory(new PropertyValueFactory<>("tenHang"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colUsernameNV.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullNameNV.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRoleNV.setCellValueFactory(new PropertyValueFactory<>("role"));

        bangHoaDon.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && bangHoaDon.getSelectionModel().getSelectedItem() != null)
                loadChiTiet(bangHoaDon.getSelectionModel().getSelectedItem().getMaHoaDon());
        });
        tableNhanVien.getSelectionModel().selectedItemProperty().addListener((o, old, nv) -> {
            if (nv != null) {
                selectedUser = nv.getUsername(); txtUsername.setText(nv.getUsername());
                txtPassword.setText(nv.getPassword()); txtFullName.setText(nv.getFullName()); cbRole.setValue(nv.getRole());
            }
        });
        loadData();
    }

    private void loadData() {
        bangHoaDon.setItems(DatabaseHelper.getDanhSachHoaDonHomNay());
        lblDoanhThu.setText("DOANH THU HÔM NAY: " + df.format(DatabaseHelper.getDoanhThuHomNay()) + " VNĐ");
        if (lblTongVeTraHomNay != null) {
            lblTongVeTraHomNay.setText("TỔNG VÉ TRẢ (TOÀN HỆ THỐNG): " + DatabaseHelper.getTongVeTraHomNay());
        }
        tableNhanVien.setItems(DatabaseHelper.getAllTaiKhoan());
    }

    private void loadChiTiet(String ma) { tableChiTietHoaDon.setItems(DatabaseHelper.getChiTietHoaDon(ma)); }

    @FXML void handleRefresh(ActionEvent e) { loadData(); }
    @FXML void handleLogout(ActionEvent e) {
        try {
            UserSession.cleanUserSession();
            Parent r = FXMLLoader.load(getClass().getResource("/com/dlvs/fxml/DangNhapView.fxml"));
            ((Stage)((Node)e.getSource()).getScene().getWindow()).setScene(new Scene(r, 400, 400));
        } catch (Exception ex) {}
    }

    @FXML void handleSaoLuu(ActionEvent e) {
        DirectoryChooser dc = new DirectoryChooser();
        File folder = dc.showDialog(((Node)e.getSource()).getScene().getWindow());
        if (folder != null) {
            try {
                Path src = Paths.get(System.getProperty("user.dir"), "database", "database_veso.db");
                Path dest = new File(folder, "Backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyy_HHmm")) + ".db").toPath();
                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                
                Alert alert = new Alert(AlertType.INFORMATION, "Đã sao lưu tại: " + dest);
                alert.setTitle("Thành công");
                alert.show();
            } catch (Exception ex) { 
                Alert alert = new Alert(AlertType.ERROR);
                alert.setContentText("Lỗi: " + ex.getMessage());
                alert.show();
            }
        }
    }

    @FXML void handleKiemTraCapNhat(ActionEvent e) {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/qthai280902/dlvs/master/version.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String v = reader.readLine().trim();
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Cập nhật");
                    alert.setContentText("Version hiện tại: 1.0. Mới nhất: " + v);
                    alert.show();
                });
            } catch (Exception ex) { }
        }).start();
    }

    @FXML void handleThemNV(ActionEvent e) { if (DatabaseHelper.insertTaiKhoan(new TaiKhoan(txtUsername.getText(), txtPassword.getText(), txtFullName.getText(), cbRole.getValue()))) loadData(); }
    @FXML void handleSuaNV(ActionEvent e) { if (DatabaseHelper.updateTaiKhoan(new TaiKhoan(txtUsername.getText(), txtPassword.getText(), txtFullName.getText(), cbRole.getValue()), selectedUser)) loadData(); }
    @FXML void handleXoaNV(ActionEvent e) { if (DatabaseHelper.deleteTaiKhoan(txtUsername.getText())) loadData(); }
    @FXML void handleLamSach(ActionEvent e) { txtUsername.clear(); txtPassword.clear(); txtFullName.clear(); selectedUser = null; }
}