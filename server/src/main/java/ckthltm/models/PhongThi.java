package ckthltm.models;

public class PhongThi {
    private long id;
    private String phongThi;
    private String diaDiem;

    public PhongThi() {
    }

    public PhongThi(long id, String phongThi, String diaDiem) {
        this.id = id;
        this.phongThi = phongThi;
        this.diaDiem = diaDiem;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhongThi() {
        return phongThi;
    }

    public void setPhongThi(String phongThi) {
        this.phongThi = phongThi;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    @Override
    public String toString() {
        return id + " " + phongThi;
    }
}
