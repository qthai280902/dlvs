package com.dlvs.controller;

import com.dlvs.dao.DatabaseHelper;
import com.dlvs.model.HoaDon;
import com.dlvs.model.TaiKhoan;
import com.dlvs.model.UserSession;
import com.dlvs.model.VeBan;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.awt.Desktop;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.stage.DirectoryChooser;
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
    private Label lblTongVeTraHomNay;

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
        
        if (lblTongVeTraHomNay != null) {
            int tongVe = DatabaseHelper.getTongVeTraToanHeThongHomNay();
            lblTongVeTraHomNay.setText("TỔNG VÉ TRẢ (TOÀN HỆ THỐNG): " + tongVe);
        }

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

    @FXML
    void handleSaoLuu(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Chọn thư mục lưu file Backup");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"));
            String backupFileName = "Backup_LocPhatTai_" + timeStamp + ".db";
            
            Path sourceDb = Paths.get(System.getProperty("user.dir"), "database", "database_veso.db");
            Path targetDb = new File(selectedDirectory, backupFileName).toPath();

            try {
                if (!Files.exists(sourceDb)) {
                    showAlert(AlertType.ERROR, "Lỗi Sao Lưu", "Không tìm thấy file CSDL gốc: " + sourceDb);
                    return;
                }
                Files.copy(sourceDb, targetDb, StandardCopyOption.REPLACE_EXISTING);
                showAlert(AlertType.INFORMATION, "Thành Công!", "Đã sao lưu dữ liệu tuyệt đối an toàn ra:\n" + targetDb.toString());
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Lỗi Sao Lưu Ngoại Lệ", "Chi tiết lỗi:\n" + e.getMessage());
            }
        }
    }

    private static final String CURRENT_VERSION = "1.0";

    @FXML
    void handleKiemTraCapNhat(ActionEvent event) {
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                URL url = new URL("https://raw.githubusercontent.com/qthai280902/dlvs/master/version.txt");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    return reader.readLine().trim();
                }
            }
        };

        task.setOnSucceeded(e -> {
            String latestVersion = task.getValue();
            if (latestVersion != null && !latestVersion.equals(CURRENT_VERSION)) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Thông báo Cập Nhật");
                alert.setHeaderText("Đã có phiên bản mới v" + latestVersion + "!");
                alert.setContentText("Bạn đang dùng v" + CURRENT_VERSION + ". Bạn có muốn mở trình duyệt để tải về ngay không?");
                
                javafx.scene.control.ButtonType btnCo = new javafx.scene.control.ButtonType("Có, tải ngay");
                javafx.scene.control.ButtonType btnKhong = new javafx.scene.control.ButtonType("Để sau", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                
                alert.getButtonTypes().setAll(btnCo, btnKhong);
                
                java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == btnCo) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://github.com/qthai280902/dlvs/releases/latest"));
                    } catch (Exception ex) {
                        showAlert(AlertType.ERROR, "Lỗi Mở Trình Duyệt", "Không thể tự động mở trình duyệt web. Vui lòng cập nhật thủ công.");
                    }
                }
            } else {
                showAlert(AlertType.INFORMATION, "Cập Nhật", "Tuyệt vời, bạn đang dùng phiên bản mới nhất (v" + CURRENT_VERSION + ").");
            }
        });

        task.setOnFailed(e -> {
            showAlert(AlertType.ERROR, "Lỗi Băng Thông Mạng", "Không thể kết nối đến máy chủ Github để kiểm tra cập nhật. Vui lòng kiểm tra kết nối mạng.");
        });

        new Thread(task).start();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
