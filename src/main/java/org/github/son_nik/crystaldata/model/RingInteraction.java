package org.github.son_nik.crystaldata.model;

public class RingInteraction {
    private String cgCg;
    private String cgIPerp;
    private String alpha;

    public RingInteraction(String cgCg, String cgIPerp, String alpha) {
        this.cgCg = cgCg;
        this.cgIPerp = cgIPerp;
        this.alpha = alpha;
    }

    // Геттеры
    public String getCgCg() { return cgCg; }
    public String getCgIPerp() { return cgIPerp; }
    public String getAlpha() { return alpha; }

    @Override
    public String toString() {
        return cgCg + "\t|\t" + cgIPerp + "\t|\t" + alpha;
    }
}
