package org.github.son_nik.crystaldata.model;

import java.util.List;

public class ParsedData {
    private List<RingInteraction> ringInteractions;
    private List<PiInteraction> piInteractions;

    public ParsedData(List<RingInteraction> ringInteractions, List<PiInteraction> piInteractions) {
        this.ringInteractions = ringInteractions;
        this.piInteractions = piInteractions;
    }

    // Геттеры
    public List<RingInteraction> getRingInteractions() { return ringInteractions; }
    public List<PiInteraction> getPiInteractions() { return piInteractions; }

    public String getFormattedResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("Analysis of Short Ring-Interactions with Cg-Cg Distances \n");
        sb.append("Cg-Cg\t|\tCgI_Perp\t|\tAlpha\n");
        for (RingInteraction ri : ringInteractions) {
            sb.append(ri.toString()).append("\n");
        }
        sb.append("\n\n");
        sb.append("Analysis of Y-X...Cg(Pi-Ring) Interactions (X..Cg < 4.0 Ang. - Gamma <  30.0 Deg) \n");
        sb.append("X..Cg\t|\tX-Perp\t|\tY-X..Cg\n");
        for (PiInteraction pi : piInteractions) {
            sb.append(pi.toString()).append("\n");
        }
        return sb.toString();
    }
}
