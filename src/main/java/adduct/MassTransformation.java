package adduct;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MassTransformation {

    private static final Pattern P_ADUCT = Pattern.compile(
                    "\\[(?:(\\d*)M)" +      // grupo(1): multímero, p.ej. "2" en "[2M+H]+"
                    "([^\\]]+)\\]" +        // grupo(2): resto dentro de corchetes, p.ej. "+H" o "+H-H2O"
                    "(\\d*)([+-])"          // grupo(3): carga (número), grupo(4): signo
    );

    /**
     * A partir de un m/z observado y un aducto (clave en AdductList),
     * calcula la masa monoisotópica neutra.
     *
     * @param mz     m/z observado
     * @param adduct String exactamente igual a la clave de AdductList
     * @return masa monoisotópica neutral (M)
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        Matcher m = P_ADUCT.matcher(adduct);
        if (!m.matches()) {
            throw new IllegalArgumentException("Formato de aducto no reconocido: " + adduct);
        }
        // 1) extrae multímero
        String multStr = m.group(1);
        int mult = multStr.isEmpty() ? 1 : Integer.parseInt(multStr);

        // 2) extrae carga
        String chargeStr = m.group(3);
        int charge = chargeStr.isEmpty() ? 1 : Integer.parseInt(chargeStr);

        // 3) obtiene el delta desde AdductList (puede ser positivo o negativo)
        Double delta = AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adduct)
                ? AdductList.MAPMZPOSITIVEADDUCTS.get(adduct)
                : AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);

        // 4) calcula M = (mz * charge + delta) / mult
        return (mz * charge + delta) / mult;
    }

    /**
     * A partir de una masa monoisotópica y un aducto,
     * calcula el m/z teórico que debería observarse.
     *
     * @param monoisotopicMass masa monoisotópica neutral (M)
     * @param adduct           String exactamente igual a la clave de AdductList
     * @return m/z teórico bajo ese aducto
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {
        Matcher m = P_ADUCT.matcher(adduct);
        if (!m.matches()) {
            throw new IllegalArgumentException("Formato de aducto no reconocido: " + adduct);
        }
        // 1) extrae multímero
        String multStr = m.group(1);
        int mult = multStr.isEmpty() ? 1 : Integer.parseInt(multStr);

        // 2) extrae carga
        String chargeStr = m.group(3);
        int charge = chargeStr.isEmpty() ? 1 : Integer.parseInt(chargeStr);

        // 3) obtiene el delta desde AdductList
        Double delta = AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adduct)
                ? AdductList.MAPMZPOSITIVEADDUCTS.get(adduct)
                : AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);

        // 4) calcula mz = (mult * M – delta) / charge
        return (monoisotopicMass * mult - delta) / charge;
    }

}
