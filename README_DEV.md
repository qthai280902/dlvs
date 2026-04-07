# 💻 Hướng Dẫn Phát Triển (Developer Guide)

Tài liệu này dành riêng cho lập trình viên để phục vụ bảo trì, nâng cấp, và duy trì hệ thống code của Siêu phẩm Lộc Phát Tài.

## 1. Stack Môi Trường Yêu Cầu Đầu Vào
- **Khai báo Code:** `Java 17` (`JDK 17`) trở lên.
- **Biên dịch & Đóng Gói (Build Tool):** `Apache Maven 3.9+`.
- **Framework thiết kế:** `JavaFX 21`.

## 2. Các Lệnh Build Bọc Thép Quan Trọng

Để cho ra định dạng Portable .exe nhẹ nhàng nhất, cần luôn thực thi cấu trúc tuần tự. Tôi đã thiết lập một file chạy nhanh tên là `build_release.bat`. Nó thực thi các lệnh lõi như sau:

1. Dọn dẹp phế phẩm và xây `Fat JAR` bằng Shade Plugin (Tuyệt đối không dùng module Assembly dễ sai metadata):
   ```bash
   mvn clean package -q
   ```
2. Đóng gói JPackage tạo App Image tự vận hành:
   ```bash
   jpackage --type app-image --name LocPhatTai --input target\jpack --main-jar dlvs-1.0-SNAPSHOT-fat.jar --main-class com.dlvs.Launcher --dest target\dist
   ```

*Lưu ý: Bạn hoàn toàn có thể click đúp vào file `build_release.bat` gốc, mọi thao tác build và nhúng CSDL đều được làm tự động 100%.*

## 3. Cảnh Báo "Tử Huyệt" Phiên Bản SQLite (Cực Kỳ Quan Trọng)

Tuyệt đối KHÔNG ấn Upgrade phiên bản thư viện `org.xerial:sqlite-jdbc` lên nhánh 3.40+ (ví dụ 3.45.x hay 3.47.1.0).

- **Lý do**: Kể từ các bản mới, `sqlite-jdbc` đã thay đổi cấu trúc Native Package bên trong tệp JAR, không sử dụng kiến trúc flat thông thường. Khi Maven Shade gom file, đường dẫn Native cực hẹp của chúng sẽ bị bỏ lại. Gây ra hiện tượng chết ứng dụng ngầm vì thiếu `sqlitejdbc.dll` (`java.lang.Exception: No native library found for os.name=Windows...`). Đồng thời SQLite mới xử lý Strict URI kém linh hoạt có thể gây ngắt đường dẫn tĩnh.
- **Phiên bản an toàn vĩnh viễn:** `3.39.4.0`.

## 4. Lộ Trình Nâng Cấp Tương Lai (Roadmap v2.0)

Một số ý tưởng để ứng dụng có thể vươn tầm chuyên nghiệp lớn hơn:
1. **Module Đồng bộ Đám Mây (Cloud Sync):** Xây dựng đồng bộ file `database_veso.db` lên Google Drive thông qua rclone để giữ an toàn dữ liệu từ xa do dạng app đang là Offline POS.
2. **Module In Khổ Nhiệt Cứng:** Tích hợp bộ Engine `java.awt.print` kết hợp PDFBox hoặc hệ driver K58, K80 dành cho máy in công nghiệp (In trực tiếp không thông qua Popup Alert).
3. **Mã hóa Code-End:** Gỉải pháp đưa `BCrypt` vào hàm thiết lập `TaiKhoan` nhằm Hash thông tin Password trước khi nạp vào DB thay vì để nguyên Plain-text.
4. **API Zalo:** Thiết lập Zalo OA Mini gửi tin nhắn tự động tổng kết hóa đơn của từng khách buôn vé số vào buổi chiều tối.
