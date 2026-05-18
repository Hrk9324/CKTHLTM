package ckthltm.models;

public class YeuCau {
    private long id;
    private int soCanBo;
    private int soPhongThi;
    private int line;

    public YeuCau() {
    }

    public YeuCau(int soCanBo, int soPhongThi, int line) {
        this.soCanBo = soCanBo;
        this.soPhongThi = soPhongThi;
        this.line = line;
    }

    public YeuCau(int id, int soCanBo, int soPhongThi, int line) {
        this.id = id;
        this.soCanBo = soCanBo;
        this.soPhongThi = soPhongThi;
        this.line = line;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSoCanBo() {
        return soCanBo;
    }

    public void setSoCanBo(int soCanBo) {
        this.soCanBo = soCanBo;
    }

    public int getSoPhongThi() {
        return soPhongThi;
    }

    public void setSoPhongThi(int soPhongThi) {
        this.soPhongThi = soPhongThi;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

}
