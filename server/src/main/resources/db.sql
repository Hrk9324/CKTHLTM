-- Tạo cơ sở dữ liệu (nếu chưa có)
CREATE DATABASE IF NOT EXISTS quan_ly_phong_thi;
USE quan_ly_phong_thi;

-- 1. Tạo bảng cho CanBo
CREATE TABLE IF NOT EXISTS can_bo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_gv VARCHAR(20) NOT NULL,
    ho_ten VARCHAR(100) NOT NULL,
    ngay_sinh VARCHAR(20),
    don_vi VARCHAR(100)
);

-- 2. Tạo bảng cho PhongThi
CREATE TABLE IF NOT EXISTS phong_thi (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phong_thi VARCHAR(50) NOT NULL,
    dia_diem VARCHAR(100) NOT NULL
);

-- 3. Tạo bảng cho YeuCau
CREATE TABLE IF NOT EXISTS yeu_cau (
    id INT AUTO_INCREMENT PRIMARY KEY,
    so_can_bo INT NOT NULL,
    so_phong_thi INT NOT NULL,
    line INT NOT NULL
);

-- 4. Tạo bảng cho GiamSat
CREATE TABLE IF NOT EXISTS giam_sat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ma_gv VARCHAR(20) NOT NULL,
    phong_thi VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX idx_unique_giam_sat 
ON giam_sat(ma_gv, phong_thi);