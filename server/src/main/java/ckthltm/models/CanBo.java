package ckthltm.models;

public class CanBo {
    private long id;
    private String maGV;
    private String hoTen;
    private String ngaySinh;
    private String donViCongTac;

    public CanBo() {
    }

    public CanBo(long id, String maGV, String hoTen, String ngaySinh, String donViCongTac) {
        this.id = id;
        this.maGV = maGV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.donViCongTac = donViCongTac;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getDonViCongTac() {
        return donViCongTac;
    }

    public void setDonViCongTac(String donViCongTac) {
        this.donViCongTac = donViCongTac;
    }

    @Override
    public String toString() {
        return id + " " + hoTen;
    }
}
