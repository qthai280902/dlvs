package com.dlvs.controller;

import com.dlvs.dao.DatabaseHelper;
import com.dlvs.model.UserSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DangNhapController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnDangNhap;

    @FXML
    void handleDangNhap(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Lỗi", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        try {
            String sql = "SELECT fullName, role FROM TaiKhoan WHERE username = ? AND password = ?";
            
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String fullName = rs.getString("fullName");
                    String role = rs.getString("role");
                    
                    UserSession.setInstance(username, fullName, role);
                    
                    Stage currentStage = (Stage) btnDangNhap.getScene().getWindow();
                    
                    if ("ADMIN".equals(role)) {
                        chuyenManHinh(currentStage, "/com/dlvs/fxml/DashboardView.fxml",
                            "ĐẠI LÝ VÉ SỐ LỘC PHÁT TÀI - QUẢN LÝ", 1000, 700);
                    } else {
                        chuyenManHinh(currentStage, "/com/dlvs/fxml/ThuNganView.fxml",
                            "ĐẠI LÝ VÉ SỐ LỘC PHÁT TÀI - THU NGÂN", 1000, 700);
                    }
                    
                } else {
                    showAlert(AlertType.ERROR, "Thất bại", "Tài khoản hoặc mật khẩu không chính xác!");
                }
            }

        } catch (Throwable e) {
            // HIỆN THỊ LỖI RA POPUP - không được dùng printStackTrace vì .exe không có console!
            Alert errAlert = new Alert(AlertType.ERROR);
            errAlert.setTitle("LỖI HỆ THỐNG");
            errAlert.setHeaderText("Exception được bắt:");
            errAlert.setContentText(e.toString() + "\n\n" + e.getMessage());
            errAlert.showAndWait();
        }
    }

    private void chuyenManHinh(Stage stage, String fxmlPath, String title, int width, int height) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root, width, height);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
