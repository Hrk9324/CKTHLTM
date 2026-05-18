package ckthltm.models.result;

import java.util.List;
import ckthltm.models.*;

public class PhanCongGiamSat {
    private CanBo canBo;
    private List<String> phongThiList;

    public PhanCongGiamSat(CanBo canBo, List<String> phongThiList) {
        this.canBo = canBo;
        this.phongThiList = phongThiList;
    }

    public CanBo getCanBo() {
        return canBo;
    }

    public List<String> getPhongThiList() {
        return phongThiList;
    }
}
