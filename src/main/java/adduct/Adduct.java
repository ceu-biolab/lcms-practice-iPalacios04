package adduct;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    private static final Pattern MULTIMER_P = Pattern.compile("^\\[(\\d*)M");
    private static final Pattern CHARGE_P  = Pattern.compile("\\](\\d*)([+-])$");


    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param mz mz
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return the monoisotopic mass of the experimental mass mz with the adduct @param adduct
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {

        // Selecciono el shift (masa del aducto) de la lista positiva o negativa
        Map<String,Double> map = AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adduct)
                ? AdductList.MAPMZPOSITIVEADDUCTS
                : AdductList.MAPMZNEGATIVEADDUCTS;
        double shift = map.get(adduct);

        // Extraigo multímero (número antes de la M; si está vacío, 1)
        Matcher mm = MULTIMER_P.matcher(adduct);
        int multimer = (mm.find() && !mm.group(1).isEmpty())
                ? Integer.parseInt(mm.group(1))
                : 1;

        // Extraigo carga (número antes de +/−; si está vacío, 1)
        Matcher mc = CHARGE_P.matcher(adduct);
        int charge = 1;
        if (mc.find() && !mc.group(1).isEmpty()) {
            charge = Integer.parseInt(mc.group(1));
        }

        // Fórmula: M = (mz * carga + shift) / multímero
        return (mz * charge + shift) / multimer;
    }


    /**
     * Calculate the mz of a monoisotopic mass with the corresponding adduct
     *
     * @param monoisotopicMass
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {

        Map<String,Double> map = AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adduct)
                ? AdductList.MAPMZPOSITIVEADDUCTS
                : AdductList.MAPMZNEGATIVEADDUCTS;
        double shift = map.get(adduct);

        Matcher mm = MULTIMER_P.matcher(adduct);
        int multimer = (mm.find() && !mm.group(1).isEmpty())
                ? Integer.parseInt(mm.group(1))
                : 1;

        Matcher mc = CHARGE_P.matcher(adduct);
        int charge = 1;
        if (mc.find() && !mc.group(1).isEmpty()) {
            charge = Integer.parseInt(mc.group(1));
        }

        // Fórmula: mz = (monoMass * multímero - shift) / carga
        return (monoisotopicMass * multimer - shift) / charge;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param theoreticalMass Theoretical mass of the compound
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000
                / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param ppm ppm of tolerance
     */
    public static double calculateDeltaPPM(Double experimentalMass, int ppm) {
        double deltaPPM;
        deltaPPM =  Math.round(Math.abs((experimentalMass * ppm) / 1000000));
        return deltaPPM;

    }

}
