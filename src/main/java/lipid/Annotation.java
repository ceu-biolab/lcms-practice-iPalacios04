package lipid;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import adduct.AdductList;
import java.util.*;
import java.util.Map.Entry;
import adduct.MassTransformation;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private final IoniationMode ionizationMode;
    private String adduct;
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;


    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode) {
        this(lipid, mz, intensity, retentionTime, ionizationMode, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.ionizationMode = ionizationMode;
        this.groupedSignals = new TreeSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;

        detectAdduct(DEFAULT_TOLERANCE);
    }


    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public IoniationMode getIonizationMode() {
        return ionizationMode;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }



    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // !CHECK Take into account that the score should be normalized between -1 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }

    /**
     * @return El score normalizado entre -1.0 y 1.0.
     *         Si no se ha aplicado ninguna regla, devuelve 0.0.
     */
    public double getNormalizedScore() {
        if (totalScoresApplied == 0) {
            return 0.0;
        }
        return (double) score / totalScoresApplied;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }
    private static final double DEFAULT_TOLERANCE = 0.01;

    /**
     * Detecta el aducto que mejor “explica” el conjunto de picos agrupados.
     * Para cada candidato X:
     *   1) infiere la masa M con getMonoisotopicMassFromMZ(mzExp, X)
     *   2) simula cada pico con getMZFromMonoisotopicMass(M, Y) para todos Y
     *   3) cuenta cuántos picos reales quedan explicados (± tolDa)
     * El X que más picos explica es el aducto detectado.
     *
     * @param tolDa tolerancia en Da para considerar un pico “explicado”
     */
    private void detectAdduct(double tolDa) {
        double mzExp = this.mz;
        Map<String,Double> candidatos = ionizationMode == IoniationMode.POSITIVE
                ? AdductList.MAPMZPOSITIVEADDUCTS
                : AdductList.MAPMZNEGATIVEADDUCTS;

        String bestAdduct = defaultAdduct();
        int    bestScore  = -1;

        for (String x : candidatos.keySet()) {
            // 1) infiero la masa neutra M a partir del pico mzExp y el aducto de prueba X
            double neutralMass = MassTransformation
                    .getMonoisotopicMassFromMZ(mzExp, x);

            int score = 0;
            // 2) recorro cada pico agrupado
            for (Peak p : groupedSignals) {
                double pMz = p.getMz();
                // salto el propio mzExp
                if (Math.abs(pMz - mzExp) < 1e-8) {
                    continue;
                }
                // 3) compruebo si **algún** aducto Y explica este pico
                for (String y : candidatos.keySet()) {
                    double mzTheoY = MassTransformation
                            .getMZFromMonoisotopicMass(neutralMass, y);
                    if (Math.abs(mzTheoY - pMz) <= tolDa) {
                        score++;
                        break;
                    }
                }
            }
            // 4) me quedo con el X que más picos explica
            if (score > bestScore) {
                bestScore  = score;
                bestAdduct = x;
            }
        }

        this.adduct = bestAdduct;
    }

    public String getAdduct() {
        return adduct;
    }

    /** Devuelve el aducto “por defecto” según el modo de ionización */
    private String defaultAdduct() {
        if (ionizationMode == IoniationMode.POSITIVE) {
            return AdductList.MAPMZPOSITIVEADDUCTS.keySet().iterator().next();
        } else {
            return AdductList.MAPMZNEGATIVEADDUCTS.keySet().iterator().next();
        }
    }

}
