package com.dlvs;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.dlvs.dao.DatabaseHelper;

/**
 * Entry point của ứng dụng JavaFX - DLVS
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Khởi tạo CSDL SQLite ngay khi ứng dụng mở
        DatabaseHelper.initializeDatabase();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/dlvs/fxml/DangNhapView.fxml"));
            Scene scene = new Scene(root, 400, 400);
            stage.setTitle("ĐẠI LÝ VÉ SỐ LỘC PHÁT TÀI");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
