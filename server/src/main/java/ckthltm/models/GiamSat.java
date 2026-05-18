package ckthltm.models;

public class GiamSat {
    private long id;
    private String maGV;
    private String phongThi;

    public GiamSat() {
    }

    public GiamSat(long id, String maGV, String phongThi) {
        this.id = id;
        this.maGV = maGV;
        this.phongThi = phongThi;
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

    public String getPhongThi() {
        return phongThi;
    }

    public void setPhongThi(String phongThi) {
        this.phongThi = phongThi;
    }

    @Override
    public String toString() {
        return id + " " + maGV + " " + phongThi;
    }
}
