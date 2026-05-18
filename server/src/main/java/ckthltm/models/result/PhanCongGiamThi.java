package ckthltm.models.result;

public class PhanCongGiamThi {
    private String phongThi;
    private String maGiamThi1;
    private String maGiamThi2;

    public PhanCongGiamThi(String phongThi, String maGiamThi1, String maGiamThi2) {
        this.phongThi = phongThi;
        this.maGiamThi1 = maGiamThi1;
        this.maGiamThi2 = maGiamThi2;
    }

    public String getPhongThi() {
        return phongThi;
    }

    public String getMaGiamThi1() {
        return maGiamThi1;
    }

    public String getMaGiamThi2() {
        return maGiamThi2;
    }

}
