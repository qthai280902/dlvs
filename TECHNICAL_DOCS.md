# 📚 Tài Liệu Kỹ Thuật (Technical Documentation)

Dự án **Đại Lý Vé Số Lộc Phát Tài** là một hệ thống quản lý bán hàng (POS) được thiết kế đặc thù cho mô hình kinh doanh đại lý vé số. Hệ thống tập trung vào tính tương tác nhanh gọn, dễ triển khai, không yêu cầu cài đặt (Portable 100%).

## 1. Kiến Trúc Ứng Dụng (Architecture)

Hệ thống được xây dựng trên nền tảng công nghệ Java hiện đại với mô hình kiến trúc MVC (Model - View - Controller).

| Thành phần | Công nghệ sử dụng | Chức năng |
| :--- | :--- | :--- |
| **Giao diện (View)** | `JavaFX 21` + `FXML` | Render giao diện người dùng mượt mà, hỗ trợ thiết kế tách biệt qua SceneBuilder. |
| **Logic & Điều khiển (Controller)** | `Java 17` | Xử lý logic nghiệp vụ, quản lý phiên làm việc (`UserSession`) và điều hướng luồng dữ liệu. |
| **Cơ sở dữ liệu (Model/Database)** | `SQLite` (sqlite-jdbc) | Lưu trữ vĩnh viễn dữ liệu không cần cài đặt Server (Zero-configuration). |
| **Công cụ Build & Đóng gói** | `Maven` + `maven-shade-plugin` + `jpackage` | Tạo Fat JAR và bọc thành định dạng phân phối `.exe` độc lập trên Windows. |

## 2. Giải Pháp "Bọc Thép" Database Độc Quyền (Portable 100%)

Để giúp hệ thống có thể chạy trên mọi phiên bản Windows ngay lập tức mà **không cần quyền Administrator** (tránh lệnh chặn của UAC/Windows Defender) và không báo lỗi sập JVM (như `UnsatisfiedLinkError` hay `Error opening connection`), chúng tôi áp dụng chiến lược thiết kế "Bọc thép":

1. **Hữu cơ hóa Native DLL (Downgrade SQLite về 3.39.4.0):**  
   Các bản SQLite mới ẩn DLL Native quá sâu khiến bộ tạo Fat JAR (Shade Plugin) dễ bỏ sót. Phiên bản `3.39.4.0` đảm bảo file Native `.dll` được Maven gom vào JAR chuẩn file "phẳng" (flat structure).
2. **Cơ chế tự chủ Temp Dir (`org.sqlite.tmpdir`):**
   Thay vì để hệ thống ép bung thư viện `.dll` vào vùng `%TEMP%` cục bộ của Windows (thường bị các trình Antivirus soi xét hoặc bị kẹt quyền UAC), chúng ta ép tĩnh hệ thống bắt buộc bung `.dll` vào thư mục `database/` đi kèm sinh ra từ thư mục mẹ `.exe`:
   ```java
   File dbFolder = new File(System.getProperty("user.dir"), "database");
   System.setProperty("org.sqlite.tmpdir", dbFolder.getAbsolutePath());
   ```
3. **Cơ chế xử lý URI Unicode chống nhầm lẫn:**
   Luôn Escape mã đường dẫn thay `\` bằng `/` (`C:/Users/..`) để tránh việc Engine URI của SQLite hiểu nhầm dãy `\U` là định dạng Escape Unicode. Mã này giúp ứng dụng tải "mượt mà" đường tới cấu trúc Database dù đặt ở bất kỳ cấp thư mục nào.

## 3. Cấu Trúc Cơ Sở Dữ Liệu

Dự án có những thực thể chính sau (xem mô tả khởi tạo trong `DatabaseHelper.java`):

- **Bảng `TaiKhoan`**: Quản trị tài khoản, cấp quyền.
  - Cột: `username` (PK), `password`, `fullName`, `role` (ADMIN/NHANVIEN).
- **Bảng `HoaDon`**: Thông tin tổng hợp mỗi lần xuất phiếu bán.
  - Cột: `maHoaDon` (PK), `ngayTao`, `tenNhanVien`, `tenKhachHang`, `tongTienHang`, `khachDua`, `tienThoi`.
- **Bảng `ChiTietHoaDon`**: (Tham chiếu đến `HoaDon`). Các vé được bán chi tiết.
  - Cột: `id` (PK), `maHoaDon` (FK), `tenHang`, `soLuong`, `donGia`, `chietKhau`, `thanhTien`.
- **Bảng `KhoVe` / `KhachHang`**: Quản lý tổng nguồn vé nhập vào và các khách nợ/đại lý cấp 2. (Extension module)
