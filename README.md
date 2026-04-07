# 🎰 ĐẠI LÝ VÉ SỐ LỘC PHÁT TÀI
### _"Mua Vé Số - Trúng Lớn - Lộc Phát Tài!"_

---

## 📌 Giới thiệu

**Phần mềm Quản lý Bán Vé Số LỘC PHÁT TÀI (POS v1.0)** là hệ thống quản lý bán hàng chuyên dụng cho đại lý vé số, được phát triển với giao diện hiện đại, dễ sử dụng và hoạt động hoàn toàn **offline**.

---

## ✨ Tính năng chính

| Tính năng | Mô tả |
|---|---|
| 🔐 Đăng nhập phân quyền | Riêng biệt cho **Sếp (Admin)** và **Thu ngân (Nhân viên)** |
| 🧾 Bán hàng & In biên lai | Nhập vé, tính tiền bối tự động, in mẫu bill POS nhiệt |
| 📊 Dashboard Sếp | Thống kê doanh thu ngày, xem chi tiết từng hóa đơn |
| 👥 Quản lý nhân sự | Thêm, sửa, xóa tài khoản nhân viên (CRUD) |
| 📈 Thống kê cá nhân | Nhân viên xem doanh số riêng, lịch sử bán hàng |
| 📚 Hướng dẫn sử dụng | Tích hợp sẵn trong ứng dụng (Tab Trợ giúp) |

---

## 👤 Hướng dẫn Đăng nhập

### 🔑 Tài khoản mặc định

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Sếp (Admin) | `admin` | `admin` |
| Thu ngân | `nhanvien` | `123456` |

> ⚠️ Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu!

---

## 🏪 Hướng dẫn Sếp (Admin)

### Tab 1 - Thống Kê & Hóa Đơn
1. Xem **doanh thu hôm nay** ở phần đầu màn hình.
2. **Click đúp (double-click)** vào một hóa đơn để xem chi tiết danh sách vé.
3. Bấm **"Làm mới dữ liệu"** để cập nhật số liệu mới nhất.

### Tab 2 - Quản Lý Nhân Sự
1. **Thêm nhân viên mới**: Điền đầy đủ thông tin vào form bên trái → Bấm **"Thêm"**.
2. **Sửa thông tin**: Click vào nhân viên ở bảng bên phải (form tự điền) → Chỉnh sửa → Bấm **"Sửa"**.
3. **Xóa tài khoản**: Click chọn nhân viên → Bấm **"Xóa"**.
4. Bấm **"Làm sạch form"** để xóa trắng ô nhập liệu.

> 📝 **Lưu ý**: Không thể xóa tài khoản `admin` gốc!

---

## 💰 Hướng dẫn Thu ngân (Nhân viên)

### Tab 1 - Bán Hàng
1. Nhập **Tên Đài**, **Số lượng**, **Đơn giá** (và Chiết khấu nếu có).
2. Bấm **"Thêm vào giỏ"** để ghi nhận từng loại vé.
3. Nhập **Tên khách hàng** và **Tiền khách đưa** ở khu vực bên phải.
4. Bấm **"Thanh toán & In biên lai"** để hoàn tất giao dịch.

### Tab 2 - Thống Kê Cá Nhân
- Xem **doanh số bán hàng của mình** trong ngày hôm nay.
- Xem **lịch sử từng hóa đơn** đã xuất.
- Bấm **"Làm mới"** để cập nhật.
- Bấm **"Đăng xuất"** để trở về màn hình đăng nhập.

---

## 📞 Hỗ trợ kỹ thuật

Gặp sự cố hoặc cần hỗ trợ, vui lòng liên hệ:

- 📱 **Hotline 1:** `0785423259`
- 📱 **Hotline 2:** `0976814652`

---

## 🗃️ Lưu ý về Database

File dữ liệu được lưu tại: `database/database_veso.db`

> 🚨 **TUYỆT ĐỐI KHÔNG XÓA** file và thư mục `database` này! Đây là toàn bộ dữ liệu hóa đơn và tài khoản của hệ thống.

---

*Phiên bản: LỘC PHÁT TÀI POS v1.0 | Bản quyền thuộc Đại Lý Vé Số Lộc Phát Tài*
