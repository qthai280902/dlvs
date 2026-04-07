package com.dlvs.controller;

import com.dlvs.dao.DatabaseHelper;
import com.dlvs.model.HoaDon;
import com.dlvs.model.TaiKhoan;
import com.dlvs.model.UserSession;
import com.dlvs.model.VeBan;
import java.io.IOException;
import java.text.DecimalFormat;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class DashboardController {

    // TAB 1: Thống Kê & Hóa Đơn
    @FXML
    private Label lblDoanhThu;

    @FXML
    private TableView<HoaDon> bangHoaDon;
    @FXML
    private TableColumn<HoaDon, String> colMaHoaDon;
    @FXML
    private TableColumn<HoaDon, String> colNgayTao;
    @FXML
    private TableColumn<HoaDon, String> colTenNhanVien;
    @FXML
    private TableColumn<HoaDon, String> colTenKhachHang;
    @FXML
    private TableColumn<HoaDon, Double> colTongTien;

    @FXML
    private TableView<VeBan> tableChiTietHoaDon;
    @FXML
    private TableColumn<VeBan, String> colTenHang;
    @FXML
    private TableColumn<VeBan, Integer> colSoLuong;
    @FXML
    private TableColumn<VeBan, Double> colDonGia;
    @FXML
    private TableColumn<VeBan, Double> colThanhTien;

    // TAB 2: Quản Lý Nhân Sự
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtFullName;
    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private TableView<TaiKhoan> tableNhanVien;
    @FXML
    private TableColumn<TaiKhoan, String> colUsernameNV;
    @FXML
    private TableColumn<TaiKhoan, String> colFullNameNV;
    @FXML
    private TableColumn<TaiKhoan, String> colRoleNV;

    private String selectedUsernameToEdit = null;

    private DecimalFormat currencyFormat = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        // --- INIT TAB 1 ---
        colMaHoaDon.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTenNhanVien.setCellValueFactory(new PropertyValueFactory<>("tenNhanVien"));
        colTenKhachHang.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTienHang"));

        colTongTien.setCellFactory(tc -> new TableCell<HoaDon, Double>() {
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

        // Init Bảng Chi Tiết Hóa Đơn
        colTenHang.setCellValueFactory(new PropertyValueFactory<>("tenHang"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));

        colDonGia.setCellFactory(tc -> new TableCell<VeBan, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(currencyFormat.format(price));
            }
        });
        colThanhTien.setCellFactory(tc -> new TableCell<VeBan, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(currencyFormat.format(price));
            }
        });

        // Bắt sự kiện double-click bảng Hóa đơn
        bangHoaDon.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && bangHoaDon.getSelectionModel().getSelectedItem() != null) {
                HoaDon selected = bangHoaDon.getSelectionModel().getSelectedItem();
                loadChiTietHoaDon(selected.getMaHoaDon());
            }
        });

        // --- INIT TAB 2 ---
        colUsernameNV.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullNameNV.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRoleNV.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Bắt sự kiện click bảng Tài khoản
        tableNhanVien.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUsernameToEdit = newSelection.getUsername();
                txtUsername.setText(newSelection.getUsername());
                txtPassword.setText(newSelection.getPassword());
                txtFullName.setText(newSelection.getFullName());
                cbRole.setValue(newSelection.getRole());
            }
        });

        // Lần đầu load dữ liệu
        loadData();
    }

    private void loadData() {
        // Tab 1
        ObservableList<HoaDon> danhSachHD = DatabaseHelper.getDanhSachHoaDonHomNay();
        bangHoaDon.setItems(danhSachHD);
        double doanhThu = DatabaseHelper.getDoanhThuHomNay();
        lblDoanhThu.setText("DOANH THU HÔM NAY: " + currencyFormat.format(doanhThu) + " VNĐ");

        // Clear details
        tableChiTietHoaDon.setItems(null);

        // Tab 2
        ObservableList<TaiKhoan> danhSachTK = DatabaseHelper.getAllTaiKhoan();
        tableNhanVien.setItems(danhSachTK);
    }

    private void loadChiTietHoaDon(String maHD) {
        ObservableList<VeBan> details = DatabaseHelper.getChiTietHoaDon(maHD);
        tableChiTietHoaDon.setItems(details);
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadData();
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

    // --- LOGIC CRUD TÀI KHOẢN ---

    @FXML
    void handleLamSach(ActionEvent event) {
        txtUsername.clear();
        txtPassword.clear();
        txtFullName.clear();
        cbRole.setValue("NHANVIEN");
        selectedUsernameToEdit = null;
        tableNhanVien.getSelectionModel().clearSelection();
    }

    @FXML
    void handleThemNV(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();
        String name = txtFullName.getText();
        String role = cbRole.getValue();

        if (user.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            showAlert(AlertType.WARNING, "Lỗi Nhập Liệu", "Vui lòng nhập đầy đủ Username, Password và FullName!");
            return;
        }

        TaiKhoan tk = new TaiKhoan(user, pass, name, role);
        if (DatabaseHelper.insertTaiKhoan(tk)) {
            showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm nhân viên mới thành công!");
            loadData();
            handleLamSach(null);
        } else {
            showAlert(AlertType.ERROR, "Lỗi DB", "Không thể thêm nhân viên. Username có thể đã tồn tại.");
        }
    }

    @FXML
    void handleSuaNV(ActionEvent event) {
        String newUsername = txtUsername.getText();
        String pass = txtPassword.getText();
        String name = txtFullName.getText();
        String role = cbRole.getValue();

        if (selectedUsernameToEdit == null || selectedUsernameToEdit.isEmpty()) {
            showAlert(AlertType.WARNING, "Lỗi Nhập Liệu", "Vui lòng chọn một nhân viên từ bảng để sửa!");
            return;
        }

        if (newUsername.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            showAlert(AlertType.WARNING, "Lỗi Nhập Liệu", "Vui lòng điền đủ thông tin!");
            return;
        }

        TaiKhoan tk = new TaiKhoan(newUsername, pass, name, role);
        if (DatabaseHelper.updateTaiKhoan(tk, selectedUsernameToEdit)) {
            showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin tài khoản thành công!");
            loadData();
            handleLamSach(null);
        } else {
            showAlert(AlertType.ERROR, "Lỗi DB", "Không thể cập nhật! Có thể Username mới đã bị trùng.");
        }
    }

    @FXML
    void handleXoaNV(ActionEvent event) {
        String user = txtUsername.getText();
        if (user.isEmpty()) {
            showAlert(AlertType.WARNING, "Lỗi Nhập Liệu", "Vui lòng chọn User cần xóa!");
            return;
        }

        if (user.equals("admin")) {
           showAlert(AlertType.WARNING, "Lỗi Quyền Hạn", "Không thể xóa tài khoản admin gốc!");
           return;
        }

        if (DatabaseHelper.deleteTaiKhoan(user)) {
            showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa nhân viên!");
            loadData();
            handleLamSach(null);
        } else {
            showAlert(AlertType.ERROR, "Lỗi DB", "Không thể xóa tài khoản!");
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
