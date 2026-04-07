package com.dlvs.model;

public class HoaDon {
    private String maHoaDon;
    private String ngayTao;
    private String tenNhanVien;
    private String tenKhachHang;
    private double tongTienHang;
    private double khachDua;
    private double tienThoi;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String ngayTao, String tenNhanVien, String tenKhachHang, double tongTienHang, double khachDua, double tienThoi) {
        this.maHoaDon = maHoaDon;
        this.ngayTao = ngayTao;
        this.tenNhanVien = tenNhanVien;
        this.tenKhachHang = tenKhachHang;
        this.tongTienHang = tongTienHang;
        this.khachDua = khachDua;
        this.tienThoi = tienThoi;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public double getTongTienHang() {
        return tongTienHang;
    }

    public void setTongTienHang(double tongTienHang) {
        this.tongTienHang = tongTienHang;
    }

    public double getKhachDua() {
        return khachDua;
    }

    public void setKhachDua(double khachDua) {
        this.khachDua = khachDua;
    }

    public double getTienThoi() {
        return tienThoi;
    }

    public void setTienThoi(double tienThoi) {
        this.tienThoi = tienThoi;
    }
}
