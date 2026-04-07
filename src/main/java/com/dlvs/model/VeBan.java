package com.dlvs.model;

public class VeBan {
    private String tenHang;
    private int soLuong;
    private double donGia;
    private double chietKhau;
    private double thanhTien;

    public VeBan() {
    }

    public VeBan(String tenHang, int soLuong, double donGia, double chietKhau, double thanhTien) {
        this.tenHang = tenHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.chietKhau = chietKhau;
        this.thanhTien = thanhTien;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double getChietKhau() {
        return chietKhau;
    }

    public void setChietKhau(double chietKhau) {
        this.chietKhau = chietKhau;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }
}
