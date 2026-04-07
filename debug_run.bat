@echo off
echo Dang kiem tra va chay ung dung...
echo Luu y: Neu co loi, thong tin se duoc ghi vao file 'LOG_LOI_HE_THONG.txt'
echo.

:: Chạy lệnh Maven và đẩy toàn bộ kết quả (bao gồm cả lỗi) vào file txt
call mvn clean javafx:run > LOG_LOI_HE_THONG.txt 2>&1

if %ERRORLEVEL% neq 0 (
    echo [!] Da phat hien loi khi khoi chay. 
    echo [!] Thai hay mo file 'LOG_LOI_HE_THONG.txt' de xem chi tiet.
) else (
    echo [OK] Ung dung da chay thanh cong!
)
pause