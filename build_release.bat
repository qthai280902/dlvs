@echo off
chcp 65001 >nul
echo ========================================================
echo   DONG GOI PHAN MEM CHI NHANH - LOC PHAT TAI (PORTABLE)
echo ========================================================
echo.

echo [1/3] Dang xoa rác va build file Fat JAR...
call mvn clean package -q
if %ERRORLEVEL% neq 0 (
    echo [LÔI] Maven Build that bai! Vui long kiem tra lai Code.
    pause
    exit /b %ERRORLEVEL%
)
echo      - Build JAR thanh cong!

echo [2/3] Dang xoa thu muc dong goi cu va thiet lap JPackage...
if exist "target\jpack" rmdir /s /q "target\jpack"
if exist "target\dist\LocPhatTai" rmdir /s /q "target\dist\LocPhatTai"
mkdir "target\jpack"
copy "target\dlvs-1.0-SNAPSHOT-fat.jar" "target\jpack\" >nul

call jpackage --type app-image --name LocPhatTai --input target\jpack --main-jar dlvs-1.0-SNAPSHOT-fat.jar --main-class com.dlvs.Launcher --dest target\dist
if %ERRORLEVEL% neq 0 (
    echo [LÔI] JPackage that bai!
    pause
    exit /b %ERRORLEVEL%
)
echo      - JPackage thanh cong!

echo [3/3] Dong bo Du lieu Database goc vao ban Release...
if exist "database" (
    xcopy /E /I /Y "database" "target\dist\LocPhatTai\database" >nul
    echo      - Da Copy du lieu DB cu.
)

echo.
echo ========================================================
echo   [HOAN TAT] THANH PHAM O THU MUC: target\dist\LocPhatTai
echo ========================================================
pause
