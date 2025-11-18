package org.github.son_nik.crystaldata.model;

public class PiInteraction {
    private String xCg;
    private String xPerp;
    private String yxCg;

    public PiInteraction(String xCg, String xPerp, String yxCg) {
        this.xCg = xCg;
        this.xPerp = xPerp;
        this.yxCg = yxCg;
    }

    // Геттеры
    public String getXCg() { return xCg; }
    public String getXPerp() { return xPerp; }
    public String getYxCg() { return yxCg; }

    @Override
    public String toString() {
        return xCg + "\t|\t" + xPerp + "\t|\t" + yxCg;
    }
}
